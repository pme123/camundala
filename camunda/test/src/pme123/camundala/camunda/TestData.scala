package pme123.camundala.camunda

import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn._
import zio.{Task, UIO, ZIO, ZManaged}

import scala.collection.immutable.HashSet
import scala.io.{BufferedSource, Source}
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
      UserTask("user_task_review_tweet",
        Extensions(Map("durationMean" -> "10000", "durationSd" -> "5000")))),
    List(
      ServiceTask("service_task_send_rejection_notification",
        DelegateExpression("#{emailAdapter}"),
        Extensions(Map("KPI-Ratio" -> "Tweet Rejected"))),
      ServiceTask("service_task_publish_on_twitter",
        DelegateExpression("#{tweetAdapter}"),
        Extensions(Map("KPI-Ratio" -> "Tweet Approved")))
    ),
    startEvents = List(StartEvent("start_event_new_tweet",
      Extensions(Map("KPI-Cycle-Start" -> "Tweet Approval Time"))
    )),
    exclusiveGateways = List(ExclusiveGateway("gateway_approved",
      Extensions(Map("KPI-Cycle-End" -> "Tweet Approval Time"))
    )
    ),
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
    ),
    HashSet(
      StaticFile("createTweet.html", "bpmn/static/forms"),
      StaticFile("reviewTweet.html", "bpmn/static/forms")
    ))
}
