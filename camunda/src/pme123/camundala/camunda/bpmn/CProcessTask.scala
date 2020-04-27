package pme123.camundala.camunda.bpmn

import org.camunda.bpm.model.bpmn.impl.instance.ExtensionElementsImpl
import org.camunda.bpm.model.bpmn.instance.{ExtensionElements, Property}
import org.camunda.bpm.model.bpmn.{instance => camunda}
import pme123.camundala.model.{Extensions, ProcessTask}

sealed trait CProcessTask
  extends CIdentifiableNode {
  def tagName: String
  def camNode: camunda.Task

  def validate(maybeTask: Option[ProcessTask]): ValidateWarnings = maybeTask match {
    case None =>
      ValidateWarnings(s"There is NO a $tagName with id '$id' in your Process.")
    case Some(_) =>
      ValidateWarnings.none
  }

  def merge(maybeTask: Option[ProcessTask]): MergeTask = maybeTask match {
    case None =>
      MergeTask(camNode, ValidateWarnings(s"There is NO a $tagName with id '$id' in your Process."))
    case Some(pt) =>
     /* pt.extensions.properties
          .foreach{case (k,v) =>
            val elem =
              camNode.getExtensionElements.addExtensionElement(classOf[camunda.Property])
            elem.setName(k)
          }
*/
      MergeTask(camNode, ValidateWarnings.none)
  }
}

case class CServiceTask(camNode: camunda.ServiceTask)
  extends CProcessTask {
  val tagName = "ServiceTask"
}

case class CUserTask(camNode: camunda.UserTask)
  extends CProcessTask {
  val tagName = "UserTask"

}
