package pme123.camundala.examples

import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Deploy

import scala.collection.immutable.HashSet

package object twitter {

  val bpmn: Bpmn =
    Bpmn("TwitterDemoProcess.bpmn",
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

  val deploy: Deploy = Deploy("default", HashSet(bpmn))
}
