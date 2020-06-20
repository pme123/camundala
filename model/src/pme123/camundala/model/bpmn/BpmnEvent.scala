package pme123.camundala.model.bpmn

sealed trait BpmnEvent
  extends BpmnNode
    with HasExtProperties {
}

case class StartEvent(id: BpmnNodeId,
                      maybeForm: Option[UserTaskForm] = None,
                      extProperties: ExtProperties = ExtProperties.none
                     )
  extends BpmnEvent
    with HasForm {

}

case class EndEvent(id: BpmnNodeId,
                    extProperties: ExtProperties = ExtProperties.none,
                    inputs: Seq[InputOutput] = Nil
                   )
  extends BpmnEvent {

}


