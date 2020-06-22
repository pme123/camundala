package pme123.camundala.model.scenarios

import pme123.camundala.model.bpmn.Bpmn

case class ProcessScenario(name: String,
                           bpmn: Bpmn,
                           businessKey: String = "DefaultScenarioBusinessKey") {

  def startWith(businessKey: String): ProcessScenario = copy(businessKey = businessKey)

}
