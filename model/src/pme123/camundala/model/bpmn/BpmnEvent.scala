package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropExtensions

sealed trait BpmnEvent
  extends BpmnNode
    with Extensionable {
  def inOuts: InputOutputs = InputOutputs.none
}

case class StartEvent(id: BpmnNodeId,
                      maybeForm: Option[UserTaskForm] = None,
                      extensions: PropExtensions = PropExtensions.none
                     )
  extends BpmnEvent
    with HasForm

case class EndEvent(id: BpmnNodeId,
                    extensions: PropExtensions = PropExtensions.none,
                    inputs: Seq[InputOutput] = Nil
                   )
  extends BpmnEvent {
  override val inOuts: InputOutputs = InputOutputs(inputs)

}


