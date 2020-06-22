package pme123.camundala.model.bpmn

sealed trait BpmnEvent
  extends BpmnNode
    with HasExtProperties {
}

case class StartEvent(id: BpmnNodeId,
                      maybeForm: Option[UserTaskForm] = None,
                      extProperties: ExtProperties = ExtProperties.none,
                      outFlows: Seq[SequenceFlow] = Seq.empty
                     )
  extends BpmnEvent
    with HasForm
    with HasOutFlows {

}

case class EndEvent(id: BpmnNodeId,
                    extProperties: ExtProperties = ExtProperties.none,
                    inputs: Seq[InputOutput] = Nil,
                    inFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends BpmnEvent
    with HasInFlows {

}


