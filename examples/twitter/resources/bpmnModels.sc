
import eu.timepit.refined.auto._
import pme123.camundala.examples.common.deploys
import pme123.camundala.model.bpmn.ConditionExpression.{Expression, InlineScript}
import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions, PropInOutExtensions}
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._


val bpmns: Seq[Bpmn] =
  Seq(Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      BpmnProcess("TwitterDemoProcess",
        List(
          UserTask("user_task_review_tweet",
            Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
            PropInOutExtensions(Seq(Prop("durationMean", "10000"), Prop("durationSd", "5000"))),
            InputOutputs(Seq(InputOutput("scalascript", InlineScript("Hello from Scala"))),
              Seq(InputOutput("scalascriptOut", InlineScript("Bye Bye from Scala")))
            ))
        ),
        List(
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
        )),
        sequenceFlows = List(SequenceFlow("yes",
          Some(Expression("#{approved}")),
          PropExtensions(Seq(Prop("probability", "87"))
          )),
          SequenceFlow("no",
            Some(Expression("#{!approved}")),
            PropExtensions(Seq(Prop("probability", "13"))
            ))
        ))
    )))

deploys.standard(bpmns, s"http://localhost:9998/rest")