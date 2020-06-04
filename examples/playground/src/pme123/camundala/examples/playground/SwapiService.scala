package pme123.camundala.examples.playground

import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.examples.playground.bpmns.swapiHost
import pme123.camundala.model.bpmn.{BpmnNodeId, PathElem, ServiceTask}

case class SwapiService(category: PathElem) {

  def asServiceTask(id: BpmnNodeId): ServiceTask =
  RestServiceTempl(
    Request(
      swapiHost,
      path = Path(category),
      responseVariable = "swapiResult"
    )
  ).asServiceTask(id)
}
