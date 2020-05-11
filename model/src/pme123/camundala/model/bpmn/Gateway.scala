package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropExtensions

trait Gateway
  extends BpmnNode
    with Extensionable {
  val inOuts: InputOutputs = InputOutputs.none
}

case class ExclusiveGateway(id: String, extensions: PropExtensions = PropExtensions.none)
  extends Gateway

case class ParallelGateway(id: String, extensions: PropExtensions = PropExtensions.none)
  extends Gateway
