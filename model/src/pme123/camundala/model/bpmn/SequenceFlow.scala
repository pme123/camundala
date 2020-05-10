package pme123.camundala.model.bpmn

case class SequenceFlow(id: String,
                        maybeExpression: Option[ConditionExpression] = None,
                        extensions: Extensions = Extensions.none)
  extends BpmnNode
    with Extensionable {

}
