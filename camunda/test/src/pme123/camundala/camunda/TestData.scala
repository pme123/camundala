package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.Request.Auth.BasicAuth
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.bpmn.ops._
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
  val worker: Group =
    Group("worker")
      .name("Worker")
  val guest: Group =
    Group("guest")
      .name("Guest")
  val hans: User =
    User("hans")
      .name("Müller")
      .firstName("Hans")
      .email("hans@mueller.ch")
      .group(worker)
  val heidi: User =
    User("heidi")
      .name("Meier")
      .firstName("Heidi")
      .email("heidi@meier.ch")
      .group(guest)
  val peter: User =
    User("peter")
      .name("Arnold")
      .firstName("Peter")
      .email("peter@arnold.ch")
      .group(guest)
      .group(worker)

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

  lazy val twitterProcess: BpmnProcess =
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

  lazy val testProcess: BpmnProcess =
    BpmnProcess("TestDemoProcess"
    ).*** {
      StartEvent("startEvent")
    }.*** {
      ServiceTask("CallSwapiServiceTask")
        .external("myTopic")
    }.***{
      CallActivity("SayHelloCallActivity")
        .calledElement("TwitterDemoProcess.bpmn", subProcess)
    }.*** {
      ServiceTask("external-task-example")
        .external("myTopic")
    }.*** {
      SendTask("send-task-example")
        .external("myTopic")
    }

  lazy val subProcess: BpmnProcess =
    BpmnProcess("MySubProcess"
    ).*** {
      StartEvent("startEvent")
    }.*** {
      UserTask("SayHelloTask")
        .form(GeneratedForm()
        .---(textField("hello").default("Hi there, Welcome in the Subtask")))
    }

  val bpmn: Bpmn = Bpmn("TwitterDemoProcess.bpmn", "TwitterDemoProcess.bpmn")
    .process(twitterProcess)
    .process(testProcess)
}
