package pme123.camundala.model.bpmn

sealed trait BpmnEvent
  extends BpmnNode
    with Extensionable

case class StartEvent(id: String,
                      maybeForm: Option[UserTaskForm] = None,
                      extensions: Extensions = Extensions.none
                     )
  extends BpmnEvent
    with HasForm

case class EndEvent(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends BpmnEvent


