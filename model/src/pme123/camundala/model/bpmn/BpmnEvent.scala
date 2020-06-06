package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.{Prop, PropExtensions}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm

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
    with HasForm {

  def form(form: UserTaskForm): StartEvent = copy(maybeForm = Some(form))

  def embeddedForm(fileName: FilePath, resourcePath: PathElem): StartEvent =
    copy(maybeForm = Some(EmbeddedDeploymentForm(StaticFile(fileName, resourcePath))))

  def prop(prop: (PropKey, String)): StartEvent = copy(extensions = extensions :+ Prop(prop._1, prop._2))

}

case class EndEvent(id: BpmnNodeId,
                    extensions: PropExtensions = PropExtensions.none,
                    inputs: Seq[InputOutput] = Nil
                   )
  extends BpmnEvent {
  override val inOuts: InputOutputs = InputOutputs(inputs)

}


