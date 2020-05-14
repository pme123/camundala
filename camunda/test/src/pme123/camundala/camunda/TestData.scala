package pme123.camundala.camunda

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.ConditionExpression.Expression
import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions, PropInOutExtensions}
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._
import zio.{Task, UIO, ZIO, ZManaged}

import scala.io.Source
import scala.xml.{Elem, XML}

object TestData {

  lazy val bpmnXmlTask: Task[Elem] = {
    for {
      bpmnResource <- ZIO.effect(Source.fromResource("bpmn/TwitterDemoProcess.bpmn"))
      bpmnXml <- ZManaged.make(UIO(bpmnResource.reader()))(r => UIO(r.close()))
        .use(r => ZIO.effect(XML.load(r)))
    } yield bpmnXml

  }

  val twitterProcess: BpmnProcess = BpmnProcess("TwitterDemoProcess",
    List(
      //embedded:deployment:static/forms/reviewTweet.html
      UserTask("user_task_review_tweet",
        Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
        PropInOutExtensions(Seq(Prop("durationMean", "10000"), Prop("durationSd", "5000")),
          InputOutputs(Seq(InputOutput("testVal", Expression("ok"))))))),
    List(
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
