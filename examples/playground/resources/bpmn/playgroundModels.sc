
import eu.timepit.refined.auto._
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.examples.playground.AddressService
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint.Required
import pme123.camundala.model.bpmn.UserTaskForm.FormField._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Deploys

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
                .validate(Required)
            })
          .prop("waitForTask", "true")
      }.*** {
      AddressService(addressHost).getAddress("GetAddressTask")
    }.*** {
      UserTask("AddressChangeTask")
        .candidateGroup(maGroup)
        .inputExternal("formJson", "scripts/form-json.groovy", includes = Seq(ConditionExpression.asJson))
        .form(changeAddress)
        .outputExpression("formJson", "${formJson}")
        .outputExpression("newAddress", "${S(formJson).prop(\"newAddress\")}")
        .outputExpression("kube", "${currentUser()}")
        .prop("jsonVariable", "formJson")
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
          .---(textReadOnly("message")
            .default("Sorry we could not change the Address")
            .prop("display", "message")
            .prop("icon", "info")
          )
          .---(textReadOnly("compliance")
            .label("Not approved by:")
          )

        )
    }

  lazy val changeAddress: GeneratedForm =
    GeneratedForm()
      .---(textReadOnly("customer"))
      .---(address("existing", readOnly = true))
      .---(address("new"))

  lazy val approveAddress: GeneratedForm =
    GeneratedForm()
      .---(
        GroupField("infosGroup")
          .---(RowFieldGroup("infosGroup")
            .---(textReadOnly("customer")
              .width(8))
            .---(textReadOnly("kube")
              .width(8)))
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
      text(s"/${prefix}Address/$fieldId", readOnly)
        .label(s"#address.$fieldId")
        .validate(Required)
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

Deploys.standard(Seq(/*playgroundBpmn,*/ ChangeAddressBpmn().addressBpmn),
  Seq(heidi, kermit, adminUser),
  "examples/docker")
