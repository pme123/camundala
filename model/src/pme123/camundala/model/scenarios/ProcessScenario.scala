package pme123.camundala.model.scenarios

import pme123.camundala.model.bpmn.{Bpmn, BpmnNode, BpmnProcess, HasInFlows}

case class ProcessScenario(name: String,
                           process: BpmnProcess,
                           businessKey: String = "DefaultScenarioBusinessKey") {


  def startWith(businessKey: String): ProcessScenario = copy(businessKey = businessKey)

}

case class ScenarioStep(node: BpmnNode, nexts: Seq[BpmnNode], bag: Map[String,String])

