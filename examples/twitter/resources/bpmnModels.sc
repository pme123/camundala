import java.nio.file.Paths

import pme123.camundala.model.bpmn.ConditionExpression.{Expression, InlineScript}
import pme123.camundala.model.bpmn.Extensions.{PropExtensions, PropInOutExtensions}
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.{Deploy, Deploys, DockerConfig}

val bpmns: Set[Bpmn] =
  Set(Bpmn("TwitterDemoProcess.bpmn",
    StaticFile("TwitterDemoProcess.bpmn", "bpmn"),
    List(
      BpmnProcess("TwitterDemoProcess",
        List(
          UserTask("user_task_review_tweet",
            Some(EmbeddedDeploymentForm(StaticFile("static/forms/reviewTweet.html", "bpmn"))),
            PropInOutExtensions(Map("durationMean" -> "10000", "durationSd" -> "5000"),
              InputOutputs(Seq(InputOutput("scalascript", InlineScript("Hello from Scala"))),
                Seq(InputOutput("scalascriptOut", InlineScript("Bye Bye from Scala")))
                ))
          )),
          List(
            ServiceTask("service_task_send_rejection_notification",
              DelegateExpression("#{emailAdapter}"),
              PropInOutExtensions(Map("KPI-Ratio" -> "Tweet Rejected"))),
            ServiceTask("service_task_publish_on_twitter",
              DelegateExpression("#{tweetAdapter}"),
              PropInOutExtensions(Map("KPI-Ratio" -> "Tweet Approved")))
          ),
          startEvents = List(StartEvent("start_event_new_tweet",
            Some(EmbeddedDeploymentForm(StaticFile("static/forms/createTweet.html", "bpmn"))),
            PropExtensions(Map("KPI-Cycle-Start" -> "Tweet Approval Time"))
          )),
          exclusiveGateways = List(ExclusiveGateway("gateway_approved",
            PropExtensions(Map("KPI-Cycle-End" -> "Tweet Approval Time"))
          )
          ),
          sequenceFlows = List(SequenceFlow("yes",
            Some(Expression("#{approved}")),
            PropExtensions(Map("probability" -> "87"))
          ), SequenceFlow("no",
            Some(Expression("#{!approved}")),
            PropExtensions(Map("probability" -> "13"))
          ))
        ))
    ))

    Deploys (Set(
    Deploy("default", bpmns, DockerConfig(dockerDir = Paths.get(s"./examples/docker")))
  ))