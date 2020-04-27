package pme123.camundala.examples.twitter

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.{ZSpringApp, bpmnService, deploymentService}
import pme123.camundala.config.appConfig
import pme123.camundala.model.processRegister.ProcessRegister
import pme123.camundala.model._
import pme123.camundala.services.httpServer
import zio.console.Console
import zio.stm.TMap
import zio.{ULayer, ZIO}

@SpringBootApplication
@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- http.fork
      _ <- registerProcesses
      _ <- spring(args).useForever
    } yield ())
    .provideCustomLayer(httpServerLayer ++ bpmnServiceLayer ++ registerLayer)
      .fold(
        _ => 1,
        _ => 0
      )

  private lazy val bpmnIdMapSTM: ZIO[Any, Nothing, TMap[String, BpmnProcess]] = TMap.make[String, BpmnProcess]().commit

  private val registerLayer: ULayer[ProcessRegister] = processRegister.live
  private val bpmnServiceLayer = registerLayer >>> bpmnService.live
  private val deploymentLayer = bpmnServiceLayer >>> deploymentService.live
  private val httpServerLayer = (appConfig.live ++ deploymentLayer ++ Console.live) >>> httpServer.live

  private lazy val http = httpServer.serve()
  //  .provideCustomLayer(httpServerLayer ++ bpmnServiceLayer)

  private def spring(args: List[String]) = {
    managedSpringApp(classOf[TwitterApp], args)
    //  .provideCustomLayer(bpmnServiceLayer)
  }

  private lazy val registerProcesses = {
    processRegister.registerProcess(process)
    //  .provideCustomLayer(registerLayer)
  }

  private val process: BpmnProcess = BpmnProcess("TwitterDemoProcess",
    List(
      UserTask("user_task_review_tweet", Extensions(Map("durationMean" -> "10000", "durationSd" -> "5000")))),
    List(
      ServiceTask("service_task_send_rejection_notification"),
      ServiceTask("service_task_publish_on_twitter")
    ))
}
