package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.bpmn.ops._

import scala.annotation.nowarn

@nowarn("cat=lint-missing-interpolator")
case class ChangeAddressBpmn(maGroup: Group = adminGroup,
                             complianceGroup: Group = adminGroup,
                             addressHost: Host = Host.unknown) {

  lazy val addressBpmn: Bpmn =
    Bpmn("ChangeAddress.bpmn", "ChangeAddress.bpmn")
      .###(changeAddressProcess)

  lazy val changeAddressProcess: BpmnProcess =
    BpmnProcess("ChangeAddressDemo")
      .starterGroup(maGroup)
      .***(CustomerSearchStartEvent)
      .***(GetAddressTask)
      .***(AddressChangeTask)
      .***(CountryRiskTask)
      .***(ApprovalRequiredGateway)
      .***(NoApprovalRequiredSequenceFlow)
      .***(SaveToFCSTask)
      .***(ApprovalRequiredSequenceFlow)
      .***(ApproveAddressTask)
      .***(AddressApprovedGateway)
      .***(AddressApprovedSequenceFlow)
      .***(AddressNotApprovedSequenceFlow)
      .***(InformMATask)

  lazy val CustomerSearchStartEvent: StartEvent = StartEvent("CustomerSearchStartEvent")
    .form(GeneratedForm()
      .--- {
        enumField("customer") // replace with Lookup Source
          .label("Customer")
          .value("muller", "Peter Müller")
          .value("meier", "Heidi Meier")
          .value("arnold", "Heinrich Arnold")
          .value("schuler", "Petra Schuler")
          .value("meinrad", "Helga Meinrad")
          .required
      })
    .prop("waitForTask", "true")

  lazy val CountryRiskTask: BusinessRuleTask = BusinessRuleTask("CountryRiskTask")
    .dmn("country-risk.dmn", "approvalRequired")
    .inputExternal("currentCountry", "scripts/dmn-in-existing-country.groovy")
    .inputExternal("targetCountry", "scripts/dmn-in-new-country.groovy")

  lazy val AddressChangeTask: UserTask = UserTask("AddressChangeTask")
    .candidateGroup(maGroup)
    .inputExternal("formJson", "scripts/form-json.groovy", includes = Seq(ConditionExpression.asJson))
    .form(addressChangeForm)
    .outputExpression("formJson", "${formJson}")
    .outputExpression("newAddress", "${S(formJson).prop(\"newAddress\")}")
    .outputExpression("kube", "${currentUser()}")
    .prop("jsonVariable", "formJson")

  lazy val addressChangeForm: GeneratedForm =
    GeneratedForm()
      .---(textField("customer").readonly)
      .---(addressGroup("existing", readOnly = true))
      .---(addressGroup("new"))

  lazy val ApproveAddressTask: UserTask = UserTask("ApproveAddressTask")
    .candidateGroup(complianceGroup)
    .form(approveAddressForm)
    .outputExpression("compliance", "${currentUser()}")
    .prop("jsonVariable", "formJson")

  lazy val approveAddressForm: GeneratedForm =
    GeneratedForm()
      .---(infoGroup)
      .---(addressGroup("existing", readOnly = true))
      .---(addressGroup("new", readOnly = true))
      .---(approveButton)
      .---(disapproveButton)

  lazy val infoGroup: GroupField =
    groupField("infosGroup")
      .---(
        rowGroupField("infosGroup")
          .---(textField("customer").width(8).readonly)
          .---(textField("kube").width(8).readonly)
      )

  def addressGroup(prefix: String, readOnly: Boolean = false): GroupField = {
    def addressField(fieldId: String) = {
      val field = textField(s"/${prefix}Address/$fieldId")
        .label(s"#addressGroup.$fieldId")
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

  lazy val approveButton: SimpleField = booleanField("approveAddress")
    .prop("isPrimary", "true")
    .prop("display", "button")
  lazy val disapproveButton: SimpleField = booleanField("disapproveAddress")
    .prop("display", "button")

  lazy val InformMATask: UserTask = UserTask("InformMATask")
    .candidateGroup(maGroup)
    .form(GeneratedForm()
      .---(
        textField("message")
          .default("Sorry we could not change the Address")
          .prop("display", "message")
          .prop("icon", "info")
          .readonly
      )
      .---(
        textField("compliance")
          .label("Not approved by:")
          .readonly
      )
    )

  private lazy val GetAddressTask = AddressService(addressHost).getAddress("GetAddressTask")
  private lazy val SaveToFCSTask = AddressService(addressHost).saveAddress("SaveToFCSTask")
  private lazy val ApprovalRequiredGateway = ExclusiveGateway("ApprovalRequiredGateway")
  private lazy val AddressApprovedGateway = ExclusiveGateway("AddressApprovedGateway")

  private lazy val ApprovalRequiredSequenceFlow =
    SequenceFlow("ApprovalRequiredSequenceFlow")
      .expression("${approvalRequired}")

  private lazy val NoApprovalRequiredSequenceFlow =
    SequenceFlow("NoApprovalRequiredSequenceFlow")
      .expression("${!approvalRequired}")

  private lazy val AddressApprovedSequenceFlow =
    SequenceFlow("AddressApprovedSequenceFlow")
      .expression("${approveAddress}")

  private lazy val AddressNotApprovedSequenceFlow =
    SequenceFlow("AddressNotApprovedSequenceFlow")
      .expression("${!approveAddress}")
}
