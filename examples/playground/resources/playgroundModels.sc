
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.common.deploys
import pme123.camundala.examples.playground.SwapiService
import pme123.camundala.examples.playground.bpmns._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint.Required
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, EnumValue, EnumValues, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._

val worker: Group = Group("worker", Some("Worker"))
val guest: Group = Group("guest", Some("Guest"))
val hans: User = User("hans", Some("Müller"), Some("Hans"), Some("hans@mueller.ch"), Seq(worker))
val heidi: User = User("heidi", Some("Meier"), Some("Heidi"), Some("heidi@meier.ch"), Seq(guest))
val peter: User = User("peter", Some("Arnold"), Some("Peter"), Some("peter@arnold.ch"), Seq(guest, worker))

val swapiProcess = BpmnProcess("SwapiProcess",
  starterUsers = CandidateUsers(peter),
  starterGroups = CandidateGroups(worker),
  userTasks = List(
    UserTask("ShowResultTask",
      candidateUsers = CandidateUsers(hans),
      candidateGroups = CandidateGroups(guest),
      maybeForm = Some(GeneratedForm(Seq(SimpleField("swapiResult", "SWAPI Result", validations = Seq(Required))),
      ))
    )
  ),
  serviceTasks = List(
    RestServiceTempl(
      Request(
        swapiHost,
        path = Path("_category_/"),
        responseVariable = "swapiResult",
        mappings = Map("_category_" -> "people")
      )
    ).asServiceTask("CallSwapiServiceTask")
  ),
  sendTasks = List(),
  startEvents = List(StartEvent("DefineInputsStartEvent",
    Some(GeneratedForm(Seq(EnumField("_category_", "Category", "people",
      EnumValues(Seq(EnumValue("people", "People"),
        EnumValue("planets", "Planets"),
        EnumValue("films", "Films"),
        EnumValue("vehicles", "Vehicles"),
        EnumValue("starships", "Starships"))),
      validations = Seq(Required))))
    ))),
  exclusiveGateways = List(),
  parallelGateways = List(),
  sequenceFlows = List(SequenceFlow("SequenceFlow_9"), SequenceFlow("SequenceFlow_0m72fzi"), SequenceFlow("SequenceFlow_0k5kyka")),
)

val swapiPlanetProcess = BpmnProcess("SwapiPlanetProcess",
  userTasks = List(
    UserTask("ShowResultTask1",
      maybeForm = Some(GeneratedForm(Seq(SimpleField("swapiResult", "SWAPI Result", validations = Seq(Required))),
      )))
  ),
  serviceTasks = List(
    SwapiService("planets/").asServiceTask("CallSwapiServiceTask1")
  ),
  sendTasks = List(),
  startEvents = List(StartEvent("ShowStarWarsPlanetsStartEvent")),
  exclusiveGateways = List(),
  parallelGateways = List(),
  sequenceFlows = List(SequenceFlow("SequenceFlow_1keaeek"), SequenceFlow("SequenceFlow_0ekpwko"), SequenceFlow("SequenceFlow_1jzq3xe")),
)

val bpmns: Seq[Bpmn] =
  Seq(
    Bpmn("Playground.bpmn",
      StaticFile("Playground.bpmn", "bpmn"),
      List(
        swapiProcess,
        swapiPlanetProcess
      ))
  )

deploys.standard(bpmns, "http://localhost:10001/rest", Seq(heidi))
