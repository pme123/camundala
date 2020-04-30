package pme123.camundala.examples.twitter

import com.sun.net.httpserver.HttpServer
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.{ZSpringApp, bpmnService, deploymentService}
import pme123.camundala.config.appConfig
import pme123.camundala.model._
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.services.httpServer
import pme123.camundala.services.httpServer.HttpServer
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.{TaskLayer, ULayer, ZIO, ZLayer}

import scala.collection.immutable.HashSet

@SpringBootApplication
//@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- httpServer.serve().fork
      _ <- registerBpmns(Set(bpmn))
      _ <- managedSpringApp(classOf[TwitterApp], args).useForever
    } yield ())
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(layer)
      .fold(
        _ => 1,
        _ => 0
      )

 private  lazy val httpServerLayer = appConfigLayer ++ deploymentServiceLayer ++ logLayer("httpServer") >>> httpServer.live



  private lazy val layer = httpServerLayer ++ bpmnServiceLayer ++ bpmnRegisterLayer

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
