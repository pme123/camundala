package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.Request.Auth.BasicAuth
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.bpmn.ConditionExpression.Expression
import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions, PropInOutExtensions}
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
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
  val worker: Group = Group("worker", "Worker")
  val guest: Group = Group("guest", "Guest")
  val hans: User = User("hans", "Müller", "Hans", "hans@mueller.ch", Seq(worker))
  val heidi: User = User("heidi", "Meier", "Heidi", "heidi@meier.ch", Seq(guest))
  val peter: User = User("peter", "Arnold", "Peter", "peter@arnold.ch", Seq(guest, worker))

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

  val twitterProcess: BpmnProcess = BpmnProcess("TwitterDemoProcess",
    starterUsers = CandidateUsers(heidi, peter),
    starterGroups = CandidateGroups(worker,guest),
    userTasks = List(
      //embedded:deployment:static/forms/reviewTweet.html
      UserTask("user_task_review_tweet",
        candidateUsers = CandidateUsers(heidi),
        candidateGroups = CandidateGroups(worker),
        maybeForm = Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
        extensions = PropInOutExtensions(Seq(Prop("durationMean", "10000"), Prop("durationSd", "5000")),
          InputOutputs(Seq(InputOutput("testVal", Expression("ok"))))))),
    serviceTasks = List(
      ServiceTask("service_task_send_rejection_notification",
        DelegateExpression("#{emailAdapter}"),
        PropInOutExtensions(Seq(Prop("kpiRatio", "Tweet Rejected")))),
      ServiceTask("service_task_publish_on_twitter",
        DelegateExpression("#{tweetAdapter}"),
        PropInOutExtensions(Seq(Prop("kpiRatio", "Tweet Approved")))
      )),
    startEvents = List(StartEvent("start_event_new_tweet",
      Some(EmbeddedDeploymentForm(StaticFile("static/forms/createTweet.html", "bpmn"))),
      PropExtensions(Seq(Prop("kpiCycleStart", "Tweet Approval Time")))
    )),
    exclusiveGateways = List(ExclusiveGateway("gateway_approved",
      PropExtensions(Seq(Prop("kpiCycleStop", "Tweet Approval Time")))
    )
    ),
    sequenceFlows = List(SequenceFlow("yes",
      Some(Expression("#{approved}")),
      PropExtensions(Seq(Prop("probability", "87")))
    ), SequenceFlow("no",
      Some(Expression("#{!approved}")),
      PropExtensions(Seq(Prop("probability", "13")))
    ))
  )

  val testProcess: BpmnProcess = BpmnProcess("TestDemoProcess",
    startEvents = List(
      StartEvent("startEvent")
    ),
    serviceTasks = List(
      restServiceTask,
      ServiceTask("external-task-example",
        ExternalTask("myTopic")
      )
    ),
    sendTasks = List(
      SendTask("send-task-example",
        ExternalTask("myTopic")
      )
    )

  )

  val bpmn: Bpmn = Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      twitterProcess,
      testProcess
    ))


}
