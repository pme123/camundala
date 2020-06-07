
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.playground.SwapiService
import pme123.camundala.examples.playground.bpmns._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint.Required
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, EnumValue, EnumValues, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Deploys

val worker: Group = Group("worker", Some("Worker"))
val guest: Group = Group("guest", Some("Guest"))
val hans: User = User("hans", Some("Müller"), Some("Hans"), Some("hans@mueller.ch"), Seq(worker))
val heidi: User = User("heidi", Some("Meier"), Some("Heidi"), Some("heidi@meier.ch"), Seq(guest))
val peter: User = User("peter", Some("Arnold"), Some("Peter"), Some("peter@arnold.ch"), Seq(guest, worker))

val selectCategoryForm = GeneratedForm(Seq(EnumField("_category_", "Category", "people",
  EnumValues(Seq(EnumValue("people", "People"),
    EnumValue("planets", "Planets"),
    EnumValue("films", "Films"),
    EnumValue("vehicles", "Vehicles"),
    EnumValue("starships", "Starships"))),
  validations = Seq(Required))))

val swapiResultForm = GeneratedForm(Seq(SimpleField("swapiResult", "SWAPI Result", validations = Seq(Required))))

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
        .form(swapiResultForm)
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
