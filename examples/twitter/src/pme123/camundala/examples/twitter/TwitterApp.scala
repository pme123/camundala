package pme123.camundala.examples.twitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.ZSpringApp
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.cli.{ProjectInfo, cliApp}
import pme123.camundala.model._
import pme123.camundala.services.httpServer
import zio.ZIO
import zio.console.Console

import scala.collection.immutable.HashSet

@SpringBootApplication
//@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {
  val projectInfo: ProjectInfo =
    ProjectInfo(
      "Twitter Camundala Demo App",
      "pme123",
      "0.0.1",
      "https://github.com/pme123/camundala/tree/master/examples/twitter"
    )

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- httpServer.serve().fork
      _ <- registerBpmns(Set(bpmn))
      _ <- managedSpringApp(classOf[TwitterApp], args).useForever.fork
      _ <- runCli
    } yield ())
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(layer)
      .fold(
        _ => 1,
        _ => 0
      )

  private lazy val httpServerLayer = appConfigLayer ++ deploymentServiceLayer ++ logLayer("httpServer") >>> httpServer.live
  private lazy val cliLayer = cliApp.live

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo, List.empty)

  private lazy val layer = cliLayer ++ httpServerLayer ++ bpmnServiceLayer ++ bpmnRegisterLayer

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
