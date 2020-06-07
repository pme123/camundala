package pme123.camundala.model.bpmn

trait Gateway
  extends BpmnNode
    with HasExtProperties {
}

case class ExclusiveGateway(id: BpmnNodeId, extProperties: ExtProperties = ExtProperties.none)
  extends Gateway {

  def prop(prop: (PropKey, String)): ExclusiveGateway = copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

}

case class ParallelGateway(id: BpmnNodeId, extProperties: ExtProperties = ExtProperties.none)
  extends Gateway {

  def prop(prop: (PropKey, String)): ParallelGateway = copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

}
