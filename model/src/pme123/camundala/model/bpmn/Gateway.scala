package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions}

trait Gateway
  extends BpmnNode
    with Extensionable {
  val inOuts: InputOutputs = InputOutputs.none
}

case class ExclusiveGateway(id: BpmnNodeId, extensions: PropExtensions = PropExtensions.none)
  extends Gateway {

  def prop(prop: (PropKey, String)): ExclusiveGateway = copy(extensions = extensions :+ Prop(prop._1, prop._2))

}

case class ParallelGateway(id: BpmnNodeId, extensions: PropExtensions = PropExtensions.none)
  extends Gateway {

  def prop(prop: (PropKey, String)): ParallelGateway = copy(extensions = extensions :+ Prop(prop._1, prop._2))

}
