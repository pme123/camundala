package pme123.camundala.camunda.xml

import pme123.camundala.camunda.xml.XmlHelper.XQualifier._
import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._
import zio.{Task, UIO}

import scala.xml.{Attribute, Elem, Node, Null}

sealed trait XProcessTask[T <: ProcessTask]
  extends XBpmnNode[T] {
}

trait XImplementationTask[T <: ImplementationTask]
  extends XBpmnNode[T] {

  override def merge(maybeNode: Option[T]): Task[XMergeResult] =
    for {XMergeResult(xml, warnings) <- super.merge(maybeNode)
         result <- UIO.succeed {
           val newElem = maybeNode
             .map(_.implementation)
             .map {
               case DelegateExpression(expresssion) =>
                 xml % Attribute(camundaPrefix, delegateExpression, expresssion, Null)
               case ExternalTask(topic) =>
                 xml % Attribute(camundaPrefix, "topic", topic,
                   Attribute(camundaPrefix, "type", "external", Null))
             }.getOrElse(xml)
           XMergeResult(newElem, warnings)
         }
         } yield result
}

case class XServiceTask[T <: ServiceTask](xmlElem: Elem)
  extends XImplementationTask[T] {
  val tagName = "ServiceTask"

}

object XServiceTask {

  def unapply(node: Node): Option[Elem] = elementUnapply(node, bpmn("serviceTask"))
}

case class XSendTask[T <: SendTask](xmlElem: Elem)
  extends XImplementationTask[T] {
  val tagName = "SendTask"
}

object XSendTask {

  def unapply(node: Node): Option[Elem] = elementUnapply(node, bpmn("sendTask"))
}

trait XHasForm[T <: HasForm]
  extends XBpmnNode[T] {
  override def merge(maybeNode: Option[T]): Task[XMergeResult] =
    for {XMergeResult(xml, warnings) <- super.merge(maybeNode)
         result <- UIO.succeed {
           val newElem = maybeNode
             .flatMap(_.maybeForm)
             .map {
               case EmbeddedDeploymentForm(staticFile) =>
                 xml % Attribute(camundaPrefix, formKey, s"embedded:deployment:${staticFile.fileName}", Null)
               case _ => xml
             }.getOrElse(xml)
           XMergeResult(newElem, warnings)
         }
         } yield result
  }

case class XUserTask[T <: UserTask](xmlElem: Elem)
  extends XProcessTask[T]
with XHasForm[T] {
  val tagName = "UserTask"



}

