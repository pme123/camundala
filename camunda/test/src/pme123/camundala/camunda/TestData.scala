package pme123.camundala.camunda

import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import pme123.camundala.model.bpmn._

import scala.collection.immutable.HashSet
import scala.io.{BufferedSource, Source}

object TestData {

  val bpmnResource: BufferedSource = Source.fromResource("bpmn/TwitterDemoProcess.bpmn")

  val bpmn: Bpmn = Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      BpmnProcess("TwitterDemoProcess",
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
      )),
    HashSet(
      StaticFile("createTweet.html", "bpmn/static/forms"),
      StaticFile("reviewTweet.html", "bpmn/static/forms")
    ))
}
