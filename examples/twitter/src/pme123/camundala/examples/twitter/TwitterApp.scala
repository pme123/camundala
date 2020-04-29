package pme123.camundala.examples.twitter

import org.camunda.bpm.engine.rest.util.EngineUtil
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.{ZSpringApp, bpmnService, deploymentService}
import pme123.camundala.config.appConfig
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.model._
import pme123.camundala.services.httpServer
import zio.console.Console
import zio.stm.TMap
import zio.{ULayer, ZIO}

import scala.collection.immutable.HashSet

@SpringBootApplication
@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- http.fork
      _ <- registerBpmns(Set(bpmn))
      _ <- spring(args).useForever
    } yield ())
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(httpServerLayer ++ bpmnServiceLayer ++ registerLayer)
      .fold(
        _ => 1,
        _ => 0
      )

  private lazy val bpmnIdMapSTM: ZIO[Any, Nothing, TMap[String, Bpmn]] = TMap.make[String, Bpmn]().commit

  private lazy val processEngine = EngineUtil.lookupProcessEngine(null)

  private lazy val registerLayer: ULayer[BpmnRegister] = bpmnRegister.live
  private lazy val bpmnServiceLayer = registerLayer >>> bpmnService.live
  private lazy val deploymentLayer = bpmnServiceLayer >>> deploymentService.live(processEngine)
  private lazy val httpServerLayer = (appConfig.live ++ deploymentLayer ++ Console.live) >>> httpServer.live

  private lazy val http = httpServer.serve()

  private def spring(args: List[String]) = {
    managedSpringApp(classOf[TwitterApp], args)
  }

  private val bpmn = Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      BpmnProcess("TwitterDemoProcess",
        List(
          UserTask("user_task_review_tweet",
            Extensions(Map("durationMean" -> "10000", "durationSd" -> "5000")))),
        List(
          ServiceTask("service_task_send_rejection_notification",
            Extensions(Map("KPI-Ratio" -> "Tweet Rejected"))),
          ServiceTask("service_task_publish_on_twitter",
            Extensions(Map("KPI-Ratio" -> "Tweet Approved")))
        ),
        List(StartEvent("start_event_new_tweet",
          Extensions(Map("KPI-Cycle-Start" -> "Tweet Approval Time"))
        )),
        List(ExclusiveGateway("gateway_approved",
          Extensions(Map("KPI-Cycle-End" -> "Tweet Approval Time"))
        )
        ),
      )), HashSet(
      StaticFile("static/forms/createTweet.html", "bpmn"),
      StaticFile("static/forms/reviewTweet.html", "bpmn"),
    ))
}
