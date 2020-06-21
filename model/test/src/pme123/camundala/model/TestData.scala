package pme123.camundala.model

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.bpmn.ops._

object TestData {

  val bpmn: Bpmn =
    Bpmn("TwitterDemoProcess.bpmn", "TwitterDemoProcess.bpmn")
      .###(
        BpmnProcess("TwitterDemoProcess"
        ).***(
          StartEvent("start_event_new_tweet")
            .embeddedForm("static/forms/createTweet.html", "bpmn")
            .prop("KPI-Cycle-Start", "Tweet Approval Time")
        ).***(
          UserTask("user_task_review_tweet")
            .embeddedForm("static/forms/reviewTweet.html", "bpmn")
            .prop("durationMean", "10000")
            .prop("durationSd", "5000")
        ).***(
          ExclusiveGateway("gateway_approved")
            .prop("KPI-Cycle-End", "Tweet Approval Time")
        ).***(
          ServiceTask("service_task_send_rejection_notification")
            .delegate("#{emailAdapter}")
            .prop("KPI-Ratio", "Tweet Rejected")
        ).***(
          ServiceTask("service_task_publish_on_twitter")
            .delegate("#{tweetAdapter}")
            .prop("KPI-Ratio", "Tweet Approved")
        ))
      .###(
        BpmnProcess("TestDemoProcess")
          .*** {
            StartEvent("startEvent")
          }.*** {
          ServiceTask("external-task-example")
            .external("myTopic")
        }
      )
}
