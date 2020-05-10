import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.{Deploy, Deploys}

val bpmns: Set[Bpmn] =
  Set(Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      BpmnProcess("TwitterDemoProcess",
        List(
          UserTask("user_task_review_tweet",
            Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
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
          Some(EmbeddedDeploymentForm(StaticFile("static/forms/createTweet.html", "bpmn"))),
          Extensions(Map("KPI-Cycle-Start" -> "Tweet Approval Time"))
        )),
        exclusiveGateways = List(ExclusiveGateway("gateway_approved",
          Extensions(Map("KPI-Cycle-End" -> "Tweet Approval Time"))
        )
        ),
      ))
  ))

Deploys(Set(
  Deploy("default", bpmns)
))