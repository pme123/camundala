package pme123.camundala.model

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.Extensions.{PropExtensions, Prop, PropInOutExtensions}
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._

object TestData {

  val twitterProcess: BpmnProcess = BpmnProcess("TwitterDemoProcess",
    userTasks = List(
      //embedded:deployment:static/forms/reviewTweet.html
      UserTask("user_task_review_tweet",
        maybeForm = Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
        extensions = PropInOutExtensions(Seq(Prop("durationMean", "10000"), Prop("durationSd", "5000"))))),
    serviceTasks = List(
      ServiceTask("service_task_send_rejection_notification",
        DelegateExpression("#{emailAdapter}"),
        PropInOutExtensions(Seq(Prop("KPI-Ratio", "Tweet Rejected")))),
      ServiceTask("service_task_publish_on_twitter",
        DelegateExpression("#{tweetAdapter}"),
        PropInOutExtensions(Seq(Prop("KPI-Ratio", "Tweet Approved"))))
    ),
    startEvents = List(StartEvent("start_event_new_tweet",
      Some(EmbeddedDeploymentForm(StaticFile("static/forms/createTweet.html", "bpmn"))),
      PropExtensions(Seq(Prop("KPI-Cycle-Start", "Tweet Approval Time"))
      ))),
    exclusiveGateways = List(ExclusiveGateway("gateway_approved",
      PropExtensions(Seq(Prop("KPI-Cycle-End", "Tweet Approval Time"))
      )
    ),
    ))

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
