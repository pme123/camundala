
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.playground.{AddressService, SwapiService}
import pme123.camundala.examples.playground.bpmns._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint.Required
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Deploys

val worker: Group =
  Group("worker")
    .name("Worker")
val guest: Group =
  Group("guest")
    .name("Guest")

val hans: User =
  User("hans")
    .name("Müller")
    .firstName("Hans")
    .email("hans@mueller.ch")
    .group(worker)
val heidi: User =
  User("heidi")
    .name("Meier")
    .firstName("Heidi")
    .email("heidi@meier.ch")
    .group(guest)
val peter: User =
  User("peter")
    .name("Arnold")
    .firstName("Peter")
    .email("peter@arnold.ch")
    .group(guest)
    .group(worker)

val kermit: User =
  User("kermit")
    .group(guest)
    .group(worker)

val adminGroup: Group =
  Group("admin")
    .name("admin")
    .groupType("SYSTEM")

val userGroup: Group =
  Group("user")
    .name("user")
    .groupType("BPF")

val adminUser: User =
  User("adminUser")
    .firstName("Admin")
    .name("User")
    .group(guest)
    .group(worker)
    .group(adminGroup)
    .group(userGroup)

val selectCategoryForm =
  GeneratedForm()
    .--- {
      EnumField("_category_")
        .label("Category")
        .default("people")
        .value("people", "People")
        .value("planets", "Planets")
        .value("films", "Films")
        .value("vehicles", "Vehicles")
        .value("starships", "Starships")
        .validate(Required)
    }

val callSwapiTask = RestServiceTempl(
  Request(
    swapiHost,
    path = Path("_category_/"),
    responseVariable = "swapiResult",
    mappings = Map("_category_" -> "people")
  )
).asServiceTask("CallSwapiServiceTask")

val swapiProcess =
  BpmnProcess("SwapiProcess")
    .starterUser(peter)
    .starterGroup(worker)
    .starterGroup(adminGroup)
    .***(
      StartEvent("DefineInputsStartEvent")
        .form(selectCategoryForm)
    )
    .serviceTask(callSwapiTask)
    .***(
      UserTask("ShowResultTask")
        .candidateUser(hans)
        .candidateGroup(guest)
        .candidateGroup(adminGroup)
        .===(
          GeneratedForm()
            .--- {
              SimpleField("swapiResult")
                .label("SWAPI Result")
                .validate(Required)
            })
    )

val swapiPlanetProcess =
  BpmnProcess("SwapiPlanetProcess")
    .***(
      StartEvent("ShowStarWarsPlanetsStartEvent")
    ).***(
    SwapiService("planets/").asServiceTask("CallSwapiServiceTask1")
  ).***(
    UserTask("ShowResultTask1",
      maybeForm = Some(GeneratedForm(Seq(SimpleField("swapiResult", "SWAPI Result", validations = Seq(Required))),
      )))
  )

def changeAddressProcess(addressHost: Host = Host.unknown) =
  BpmnProcess("ChangeAddressDemo")
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
    }.*** {
    AddressService(addressHost).getAddress("GetAddressTask")
  }.*** {
    UserTask("AddressChangeTask")
  }.*** {
    BusinessRuleTask("CountryRiskTask")
      .dmn("country-risk.dmn", "approvalRequired")
  }.*** {
    ExclusiveGateway("ApprovalRequiredGateway")
  }.*** {
    SequenceFlow("NoApprovalRequiredSequenceFlow")
      .expression("${!approvalRequired}")
  }.*** {
    ServiceTask("SaveToFCSTask")
  }.*** {
    SequenceFlow("ApprovalRequiredSequenceFlow")
      .expression("${approvalRequired}")
  }.*** {
    UserTask("ApproveAddressTask")
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
  }

val playgroundBpmn: Bpmn =
  Bpmn("Playground.bpmn", "Playground.bpmn")
    .###(swapiProcess)
    .###(swapiPlanetProcess)

val addressBpmn: Bpmn =
  Bpmn("ChangeAddress.bpmn", "ChangeAddress.bpmn")
    .###(changeAddressProcess())

Deploys.standard(Seq(playgroundBpmn, addressBpmn),
  Seq(heidi, kermit, adminUser),
  "examples/docker")
