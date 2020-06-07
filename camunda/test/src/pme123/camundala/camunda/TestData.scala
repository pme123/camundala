package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.Request.Auth.BasicAuth
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.bpmn._
import zio.{Task, UIO, ZIO, ZManaged}

import scala.io.Source
import scala.xml.{Elem, XML}

object TestData {

  val url: Url = "https://swapi.dev/api"
  val host: Host = Host(url,
    BasicAuth("pme123", Sensitive("pwd123x")))
  val testRequest: Request = Request(
    host,
    path = Path("people", "1"))
  val worker: Group = Group("worker", Some("Worker"))
  val guest: Group = Group("guest", Some("Guest"))
  val hans: User = User("hans", Some("Müller"), Some("Hans"), Some("hans@mueller.ch"), Seq(worker))
  val heidi: User = User("heidi", Some("Meier"), Some("Heidi"), Some("heidi@meier.ch"), Seq(guest))
  val peter: User = User("peter", Some("Arnold"), Some("Peter"), Some("peter@arnold.ch"), Seq(guest, worker))

  val restServiceTask: ServiceTask = RestServiceTempl(
    testRequest
  ).asServiceTask("CallSwapiServiceTask")

  lazy val bpmnXmlTask: Task[Elem] = {
    for {
      bpmnResource <- ZIO.effect(Source.fromResource("bpmn/TwitterDemoProcess.bpmn"))
      bpmnXml <- ZManaged.make(UIO(bpmnResource.reader()))(r => UIO(r.close()))
        .use(r => ZIO.effect(XML.load(r)))
    } yield bpmnXml

  }

  val twitterProcess: BpmnProcess =
    BpmnProcess("TwitterDemoProcess")
      .starterGroup(worker)
      .starterGroup(guest)
      .starterUser(heidi)
      .starterUser(peter)
      .*** {
        StartEvent("start_event_new_tweet")
          .embeddedForm("static/forms/createTweet.html", "bpmn")
          .prop("kpiCycleStart", "Tweet Approval Time")
      }.*** {
      UserTask("user_task_review_tweet")
        .candidateUser(heidi)
        .candidateGroup(worker)
        .embeddedForm("static/forms/reviewTweet.html", "bpmn")
        .prop("durationMean", "10000")
        .prop("durationSd", "5000")
        .inputExpression("testVal", "ok")
    }.*** {
      ExclusiveGateway("gateway_approved")
        .prop("KPI-Cycle-End", "Tweet Approval Time")
    }.*** {
      SequenceFlow("yes")
        .expression("#{approved}")
        .prop("probability", "87")
    }.*** {
      SequenceFlow("no")
        .expression("#{!approved}")
        .prop("probability", "13")
    }.*** {
      ServiceTask("service_task_send_rejection_notification")
        .delegate("#{emailAdapter}")
        .prop("KPI-Ratio", "Tweet Rejected")
    }.*** {
      ServiceTask("service_task_publish_on_twitter")
        .delegate("#{tweetAdapter}")
        .prop("KPI-Ratio", "Tweet Approved")
    }

  val testProcess: BpmnProcess =
    BpmnProcess("TestDemoProcess"
    ).*** {
      StartEvent("startEvent")
    }.*** {
      ServiceTask("CallSwapiServiceTask")
        .external("myTopic")
    }.*** {
      ServiceTask("external-task-example")
        .external("myTopic")
    }.*** {
      SendTask("send-task-example")
        .external("myTopic")
    }

  val bpmn: Bpmn = Bpmn("TwitterDemoProcess.bpmn", "TwitterDemoProcess.bpmn")
    .process(twitterProcess)
    .process(testProcess)
}
