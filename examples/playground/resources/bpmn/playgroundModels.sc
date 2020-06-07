
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.playground.SwapiService
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
    .***(
      StartEvent("DefineInputsStartEvent")
        .form(selectCategoryForm)
    )
    .serviceTask(callSwapiTask)
    .***(
      UserTask("ShowResultTask")
        .candidateUser(hans)
        .candidateGroup(guest)
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

val bpmns: Seq[Bpmn] =
  Seq(
    Bpmn("Playground.bpmn", "Playground.bpmn")
      .process(swapiProcess)
      .process(swapiPlanetProcess)
  )

Deploys.standard(bpmns, Seq(heidi), "examples/docker")
