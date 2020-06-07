package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ConditionExpression.Expression
import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions}

case class SequenceFlow(id: BpmnNodeId,
                        maybeExpression: Option[ConditionExpression] = None,
                        extensions: PropExtensions = PropExtensions.none)
  extends BpmnNode
    with Extensionable {
  val inOuts: InputOutputs = InputOutputs.none

  def expression(expr: String): SequenceFlow = copy(maybeExpression = Some(Expression(expr)))

  def prop(prop: (PropKey, String)): SequenceFlow = copy(extensions = extensions :+ Prop(prop._1, prop._2))

}
