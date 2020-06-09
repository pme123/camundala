package pme123.camundala.camunda.xml

import pme123.camundala.camunda.xml.XmlHelper.QName._
import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn._
import zio.{IO, Task, UIO, ZIO}

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
                 xml % Attribute(camundaPrefix, delegateExpression, expresssion, camundaXmlnsAttr)
               case ExternalTask(topic) =>
                 xml % Attribute(camundaPrefix, "topic", topic,
                   Attribute(camundaPrefix, "type", "external", camundaXmlnsAttr))
             }.getOrElse(xml)
           XMergeResult(newElem, warnings)
         }
         } yield result
}

case class XServiceTask[T <: ServiceTask](xmlElem: Elem)
  extends XImplementationTask[T] {
  val tagName = "ServiceTask"

  def create(): IO[ModelException, ServiceTask] =
    for {
      nodeId <- xBpmnId
    } yield
      ServiceTask(
        nodeId
      )

}

object XServiceTask {

  def unapply(node: Node): Option[Elem] = elementUnapply(node, bpmn("serviceTask"))
}

case class XSendTask[T <: SendTask](xmlElem: Elem)
  extends XImplementationTask[T] {
  val tagName = "SendTask"

  def create(): IO[ModelException, SendTask] =
    for {
      nodeId <- xBpmnId
    } yield
      SendTask(
        nodeId
      )
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

case class XUserTask(xmlElem: Elem)
  extends XProcessTask[UserTask]
    with XHasForm[UserTask] {

  def create(): IO[ModelException, UserTask] =
    for {
      nodeId <- xBpmnId
    } yield
      UserTask(
        nodeId
      )

  val tagName = "UserTask"

  override def merge(maybeNode: Option[UserTask]): Task[XMergeResult] =
    for {XMergeResult(xml, warnings) <- super.merge(maybeNode)
         result <- maybeNode
           .map(mergeCandidates(xml, _))
           .getOrElse(ZIO.succeed(xml))
         } yield XMergeResult(result, warnings)

  private def mergeCandidates(xml: Elem, userTask: UserTask): Task[Elem] = Task {
    val candGroups = userTask.candidateGroups.asString(xml.attributeAsText(QName.camunda(candidateGroups)))
    val candUsers = userTask.candidateUsers.asString(xml.attributeAsText(QName.camunda(candidateUsers)))
    xml % Attribute(camundaPrefix, candidateGroups, candGroups,
      Attribute(camundaPrefix, candidateUsers, candUsers,
        camundaXmlnsAttr))
  }

}

