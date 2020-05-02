package pme123.camundala.model.bpmn

sealed trait BpmnEvent
  extends BpmnNode
    with Extensionable

case class StartEvent(id: String,
                      extensions: Extensions = Extensions.none
                     )
  extends BpmnEvent

case class EndEvent(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends BpmnEvent


