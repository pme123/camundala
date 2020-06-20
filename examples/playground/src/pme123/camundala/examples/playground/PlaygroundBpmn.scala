package pme123.camundala.examples.playground

import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.playground.UsersAndGroups.{adminGroup, guest, hans, peter, worker}
import pme123.camundala.examples.playground.bpmns.swapiHost
import pme123.camundala.model.bpmn.{Bpmn, BpmnProcess, StartEvent, UserTask}
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.Constraint.Required
import pme123.camundala.model.bpmn.ops._

object PlaygroundBpmn {

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
          .required
      }

  val callSwapiTask = RestServiceTempl(
    Request(
      swapiHost,
      path = Path("%category/"),
      responseVariable = "swapiResult",
      mappings = Map("category" -> "people")
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
                  .required
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

  val playgroundBpmn: Bpmn =
    Bpmn("Playground.bpmn", "Playground.bpmn")
      .###(swapiProcess)
      .###(swapiPlanetProcess)

}
