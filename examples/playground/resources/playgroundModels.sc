
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.common.deploys
import pme123.camundala.examples.playground.bpmns._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, EnumValue, EnumValues, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._

val bpmns: Set[Bpmn] =
  Set(
    Bpmn("Playground.bpmn",
      StaticFile("Playground.bpmn", "bpmn"),
      List(
        BpmnProcess("PlaygroundProcess",
          userTasks = List(
            UserTask("ShowResultTask",
              Some(GeneratedForm(Seq(SimpleField("swapiResult", "SWAPI Result"))))
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
                EnumValue("starships", "Starships"))))))
            ))),
          exclusiveGateways = List(),
          parallelGateways = List(),
          sequenceFlows = List(SequenceFlow("SequenceFlow_9"), SequenceFlow("SequenceFlow_0m72fzi"), SequenceFlow("SequenceFlow_0k5kyka")),
        )))
  )

deploys.standard(bpmns)