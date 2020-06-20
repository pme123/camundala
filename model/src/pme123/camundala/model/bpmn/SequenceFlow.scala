package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ConditionExpression.Expression

case class SequenceFlow(id: BpmnNodeId,
                        maybeExpression: Option[ConditionExpression] = None,
                        extProperties: ExtProperties = ExtProperties.none)
  extends BpmnNode
    with HasExtProperties {

  def expression(expr: String): SequenceFlow = copy(maybeExpression = Some(Expression(expr)))

}
