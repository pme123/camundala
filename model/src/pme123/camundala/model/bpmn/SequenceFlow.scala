package pme123.camundala.model.bpmn

case class SequenceFlow(id: String,
                        extensions: Extensions = Extensions.none)
  extends BpmnNode
    with Extensionable {

}
