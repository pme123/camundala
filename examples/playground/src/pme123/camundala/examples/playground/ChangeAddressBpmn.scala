package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.bpmn.UserTaskForm.FormField._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.ops._
import pme123.camundala.model.bpmn._


case class ChangeAddressBpmn(maGroup: Group = adminGroup,
                             complianceGroup: Group = adminGroup,
                             addressHost: Host = Host.unknown) {

  lazy val addressBpmn: Bpmn =
    Bpmn("ChangeAddress.bpmn", "ChangeAddress.bpmn")
      .###(changeAddressProcess)

  lazy val changeAddressProcess: BpmnProcess =
    BpmnProcess("ChangeAddressDemo")
      .starterGroup(maGroup)
      .*** {
        StartEvent("CustomerSearchStartEvent")
          .form(GeneratedForm()
            .--- {
              EnumField("customer") // replace with Lookup Source
                .label("Customer")
                .value("muller", "Peter Müller")
                .value("meier", "Heidi Meier")
                .value("arnold", "Heinrich Arnold")
                .value("schuler", "Petra Schuler")
                .value("meinrad", "Helga Meinrad")
                .required
            })
          .prop("waitForTask", "true")
      }.*** {
      AddressService(addressHost).getAddress("GetAddressTask")
    }.*** {
      addressChangeUserTask
    }.*** {
      BusinessRuleTask("CountryRiskTask")
        .dmn("country-risk.dmn", "approvalRequired")
        .inputExternal("currentCountry", "scripts/dmn-in-existing-country.groovy")
        .inputExternal("targetCountry", "scripts/dmn-in-new-country.groovy")
    }.*** {
      ExclusiveGateway("ApprovalRequiredGateway")
    }.*** {
      SequenceFlow("NoApprovalRequiredSequenceFlow")
        .expression("${!approvalRequired}")
    }.*** {
      AddressService(addressHost).saveAddress("SaveToFCSTask")
    }.*** {
      SequenceFlow("ApprovalRequiredSequenceFlow")
        .expression("${approvalRequired}")
    }.*** {
      UserTask("ApproveAddressTask")
        .candidateGroup(complianceGroup)
        .form(approveAddress)
        .outputExpression("compliance", "${currentUser()}")
        .prop("jsonVariable", "formJson")
    }.*** {
      ExclusiveGateway("AddressApprovedGateway")
    }.*** {
      SequenceFlow("AddressApprovedSequenceFlow")
        .expression("${approveAddress}")
    }.*** {
      SequenceFlow("AddressNotApprovedSequenceFlow")
        .expression("${!approveAddress}")
    }.*** {
      UserTask("InformMATask")
        .candidateGroup(maGroup)
        .form(GeneratedForm()
          .---(text("message")
            .default("Sorry we could not change the Address")
            .prop("display", "message")
            .prop("icon", "info")
            .readonly
          )
          .---(text("compliance")
            .label("Not approved by:")
            .readonly
          )

        )
    }

  lazy val addressChangeUserTask: UserTask = UserTask("AddressChangeTask")
    .candidateGroup(maGroup)
    .inputExternal("formJson", "scripts/form-json.groovy", includes = Seq(ConditionExpression.asJson))
    .form(addressChangeForm)
    .outputExpression("formJson", "${formJson}")
    .outputExpression("newAddress", "${S(formJson).prop(\"newAddress\")}")
    .outputExpression("kube", "${currentUser()}")
    .prop("jsonVariable", "formJson")

  lazy val addressChangeForm: GeneratedForm =
    GeneratedForm()
      .---(text("customer")
        .readonly)
      .---(address("existing", readOnly = true))
      .---(address("new"))

  lazy val approveAddress: GeneratedForm =
    GeneratedForm()
      .---(
        GroupField("infosGroup")
          .---(RowFieldGroup("infosGroup")
            .---(text("customer")
              .width(8)
              .readonly)
            .---(text("kube")
              .width(8)
              .readonly))
      )
      .---(address("existing", readOnly = true))
      .---(address("new", readOnly = true))
      .---(boolean("approveAddress")
        .prop("isPrimary", "true")
        .prop("display", "button"))
      .---(boolean("disapproveAddress")
        .prop("display", "button"))

  private def address(prefix: String, readOnly: Boolean = false): GroupField = {
    def addressField(fieldId: String) = {
      val field = text(s"/${prefix}Address/$fieldId")
        .label(s"#address.$fieldId")
      if (readOnly) field.readonly else field.required
    }

    GroupField(s"${prefix}AddressGroup")
      .---(addressField("street"))
      .---(RowFieldGroup(s"${prefix}CityCountry")
        .---(addressField("zipCode")
          .width(4))
        .---(addressField("city")
          .width(8))
        .---(addressField("countryIso")
          .width(4))
      )
  }

}
