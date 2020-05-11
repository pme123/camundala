package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropExtensions

case class SequenceFlow(id: String,
                        maybeExpression: Option[ConditionExpression] = None,
                        extensions: PropExtensions = PropExtensions.none)
  extends BpmnNode
    with Extensionable {
  val inOuts: InputOutputs = InputOutputs.none
}
