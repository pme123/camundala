package pme123.camundala.model

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField.{GroupField, RowGroupField}
import pme123.camundala.model.bpmn._
import pme123.camundala.model.bpmn.ops._

object TestData {

  private val testDemoProcess: BpmnProcess = BpmnProcess("TestDemoProcess")
    .*** {
      StartEvent("startEvent")
    }.*** {
    ServiceTask("external-task-example")
      .external("myTopic")
  }

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
        ).***(
          CallActivity("myTestSubProcess")
            .calledElement(testDemoProcess)
            .in("KPI-Ratio", "ratio")
        ))
      .###(
        testDemoProcess
      )

  lazy val addressChangeForm: GeneratedForm =
    GeneratedForm()
      .---(textField("customer").readonly)
      .---(addressGroup("existing", readOnly = true))
      .---(addressGroup("new"))

  def addressGroup(prefix: String, readOnly: Boolean = false): GroupField = {
    def addressField(fieldId: String) = {
      val field = textField(s"${prefix}Address$KeyDelimeter$fieldId")
        .label(s"#address.$fieldId")
      if (readOnly) field.readonly else field.required
    }

    GroupField(s"${prefix}AddressGroup")
      .---(addressField("street"))
      .---(RowGroupField(s"${prefix}CityCountry")
        .---(addressField("zipCode")
          .width(4))
        .---(addressField("city")
          .width(8))
        .---(addressField("countryIso")
          .width(4))
      )
  }
}
