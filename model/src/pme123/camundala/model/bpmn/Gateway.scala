package pme123.camundala.model.bpmn

trait Gateway
  extends BpmnNode
    with HasExtProperties {
}

case class ExclusiveGateway(id: BpmnNodeId, extProperties: ExtProperties = ExtProperties.none)
  extends Gateway {

}

case class ParallelGateway(id: BpmnNodeId, extProperties: ExtProperties = ExtProperties.none)
  extends Gateway {

}
