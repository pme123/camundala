package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm

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

  def form(form: UserTaskForm): StartEvent = copy(maybeForm = Some(form))

  def embeddedForm(fileName: FilePath, resourcePath: PathElem): StartEvent =
    copy(maybeForm = Some(EmbeddedDeploymentForm(StaticFile(fileName, resourcePath))))

  // HasExtProperties
  def prop(prop: (PropKey, String)): StartEvent =
    copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

}

case class EndEvent(id: BpmnNodeId,
                    extProperties: ExtProperties = ExtProperties.none,
                    inputs: Seq[InputOutput] = Nil
                   )
  extends BpmnEvent {

  // HasExtProperties
  def prop(prop: (PropKey, String)): EndEvent =
    copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

}


