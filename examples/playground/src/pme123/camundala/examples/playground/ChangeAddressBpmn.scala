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

  /**
    * small test case to reproduce Test Assert Problem
    * see https://forum.camunda.org/t/how-to-provide-an-external-script-in-camunda-assert/21080
    */
  lazy val ChangeAddressTestBpmn: Bpmn =
    Bpmn("ChangeAddressTest.bpmn", "ChangeAddressTest.bpmn")
      .processes(
        BpmnProcess("ChangeAddressDemo")
          .***(MyStartEvent)
          .***(
            UserTask("CustomerEditTask")
              .===(GeneratedForm()
                .---(textField("clientKey"))
              )
          ).serviceTask(GetAddressTask)
          .userTask(AddressChangeTask)
      )

  lazy val ChangeAddressBpmn: Bpmn =
    Bpmn("ChangeAddress.bpmn", "ChangeAddress.bpmn")
      .processes(
        ChangeAddressDemo
      )

  lazy val ChangeAddressDemo: BpmnProcess =
    BpmnProcess("ChangeAddressDemo")
      .starterGroups(maGroup)
      .startEvents(
        MyStartEvent
      )
      .userTasks(
        FindCustomerUserTask,
        AddressChangeTask,
        ApproveAddressTask,
        InformMATask
      )
      .serviceTasks(
        GetAddressTask,
        SaveToFCSTask
      )
      .businessRuleTasks(
        CountryRiskTask
      )
      .sendTasks(

      )
      .callActivities(
        CallActivity("FindCustomerCallActivity")
          .calledElement(FindCustomer(addressHost).process)
          .in(FindCustomer.lastname)
          .in(FindCustomer.firstname)
          .in(FindCustomer.birthday)
          .out(FindCustomer.foundCustomers)
      )
      .exclusiveGateways(
        ApprovalRequiredGateway,
        AddressApprovedGateway
      )
      .parallelGateways(

      )
      .sequenceFlows(
        SearchSequenceFlow,
        ApprovalRequiredSequenceFlow,
        NoApprovalRequiredSequenceFlow,
        AddressApprovedSequenceFlow,
        AddressNotApprovedSequenceFlow,
      )

  lazy val MyStartEvent: StartEvent =
    StartEvent("MyStartEvent")
      .prop("waitForTask", "true")

  lazy val FindCustomerUserTask: UserTask =
    UserTask("FindCustomerUserTask")
      .candidateGroup(maGroup)
      .form(GeneratedForm()
        .---(
          rowGroupField("searchGroup")
            .---(textField("search__name").required)
            .---(textField("search__firstname"))
            .---(dateField("search_birthday"))
        )
        .---(textField("foundCustomers")
          .prop("display", "tableJson")
          .prop("columns", "firstName,name,streetNo,postcodeWithPlace,clientKey")
          .prop("idColumn", "clientKey")
          .prop("sortColumn", "name")
          .prop("actions", "edit:#selectCustomer")

        )
      )
      .prop("waitForTask", "true")
      .inputExpression("foundCustomersActionValue", "")
      .outputExpression("clientKey", s"$${foundCustomersActionValue}")

  private val existingAddress: PropKey = "existingAddress"
  private val countryIso: PropKey = "countryIso"
  private val newAddress: PropKey = "newAddress"

  lazy val CountryRiskTask: BusinessRuleTask = BusinessRuleTask("CountryRiskTask")
    .dmn("country-risk.dmn", "approvalRequired")
    .inputStringFromJsonPath("currentCountry", Seq(existingAddress, countryIso))
    .inputStringFromJsonPath("targetCountry", Seq(newAddress, countryIso))

  lazy val AddressChangeTask: UserTask = UserTask("AddressChangeTask")
    .candidateGroups(maGroup, adminGroup)
    .form(addressChangeForm)
    .inputFromJson(existingAddress, addressChangeForm)
    .outputToJson(newAddress, addressChangeForm)
    .outputExpression("kube", "${currentUser()}")

  lazy val addressChangeForm: GeneratedForm =
    GeneratedForm()
      .---(textField("clientKey").readonly)
      .---(addressGroup("existing", readOnly = true))
      .---(addressGroup("new"))

  lazy val ApproveAddressTask: UserTask = UserTask("ApproveAddressTask")
    .candidateGroups(complianceGroup, adminGroup)
    .form(approveAddressForm)
    .inputFromJson(existingAddress, approveAddressForm)
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
          .---(textField("clientKey").width(8).readonly)
          .---(textField("kube").width(8).readonly)
      )

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
        .---(addressField(countryIso)
          .width(4))
      )
  }

  lazy val approveButton: SimpleField = booleanField("approveAddress")
    .prop("isPrimary", "true")
    .prop("display", "button")
  lazy val disapproveButton: SimpleField = booleanField("disapproveAddress")
    .prop("display", "button")

  lazy val InformMATask: UserTask = UserTask("InformMATask")
    .candidateGroups(maGroup, adminGroup)
    .form(GeneratedForm()
      .---(
        infoField("message", "Sorry we could not change the Address")
      )
      .---(
        textField("compliance")
          .label("Not approved by:")
          .readonly
      )
    )

  def messageField(id: String, message: String, maybeIcon: Option[String] = None): SimpleField = {
    textField(id)
      .default(message)
      .prop("display", "message")
      .prop("icon", maybeIcon)
      .readonly
  }

  def infoField(id: String, message: String): SimpleField = {
    messageField(id, message, Some("info"))
  }

  private lazy val GetAddressTask = AddressService(addressHost).getAddress("GetAddressTask")
  private lazy val SaveToFCSTask = AddressService(addressHost).saveAddress("SaveToFCSTask")
  private lazy val ApprovalRequiredGateway = ExclusiveGateway("ApprovalRequiredGateway")
  private lazy val AddressApprovedGateway = ExclusiveGateway("AddressApprovedGateway")

  private lazy val SearchSequenceFlow =
    SequenceFlow("SearchSequenceFlow")
      .expression(s"$${clientKey == null  || clientKey.trim().isEmpty()}")

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
