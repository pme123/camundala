
import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.{CamundaEndpoint, Deploy, Deploys, DockerConfig}
import pme123.camundala.model.bpmn.ops._

val bpmn: Bpmn =
    Bpmn("TwitterDemoProcess.bpmn", "TwitterDemoProcess.bpmn")
      .### {
        BpmnProcess("TwitterDemoProcess")
          .*** {
            StartEvent("start_event_new_tweet")
              .embeddedForm("static/forms/createTweet.html", "bpmn")
              .prop("KPI-Cycle-Start", "Tweet Approval Time")
          }.*** {
          UserTask("user_task_review_tweet")
            .embeddedForm("static/forms/reviewTweet.html", "bpmn")
            .prop("durationMean", "10000")
            .prop("durationSd", "5000")
            .inputInline("scalascript", "Hello from Scala")
            .outputInline("scalascriptOut", "Bye Bye from Scala")
        }.*** {
          ExclusiveGateway("gateway_approved")
            .prop("KPI-Cycle-End", "Tweet Approval Time")
        }.*** {
          SequenceFlow("yes")
            .expression("#{approved}")
            .prop("probability", "87")
        }.*** {
          SequenceFlow("no")
            .expression("#{!approved}")
            .prop("probability", "13")
        }.*** {
          ServiceTask("service_task_send_rejection_notification")
            .delegate("#{emailAdapter}")
            .prop("KPI-Ratio", "Tweet Rejected")
        }.*** {
          ServiceTask("service_task_publish_on_twitter")
            .delegate("#{tweetAdapter}")
            .prop("KPI-Ratio", "Tweet Approved")
        }
      }

lazy val devDeploy =
  Deploy()
    .bpmn(bpmn)
    .---(DockerConfig.DefaultDevConfig.dockerDir("examples/docker"))

lazy val remoteDeploy =
  devDeploy
      .id("remote")
    .---(CamundaEndpoint.DefaultRemoteEndpoint)
    .---(DockerConfig.DefaultRemoteConfig.dockerDir("examples/docker"))

Deploys()
  .+++(devDeploy)
  .+++(remoteDeploy)
