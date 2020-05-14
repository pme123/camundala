package pme123.camundala.camunda.xml

import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.bpmn.ConditionExpression.Expression
import pme123.camundala.model.bpmn.SequenceFlow
import zio.{Task, UIO}

import scala.xml.Elem

case class XSequenceFlow[T <: SequenceFlow](xmlElem: Elem)
  extends XBpmnNode[T] {
  val tagName = "SequenceFlow"

  override def merge(maybeNode: Option[T]): Task[XMergeResult] =
    for {XMergeResult(xml, warnings) <- super.merge(maybeNode)
         result <- id.map { elemId =>
           val newElem = maybeNode
             .flatMap(_.maybeExpression)
             .map {
               case Expression(value) =>
                 xml.copy(child =
                   xml.child.filterNot(_.label == "conditionExpression") ++
                   <conditionExpression xsi:type="tFormalExpression" id={s"FormalExpression_${elemId}"} xmlns:xsi={xmlnsXsi}>{value
                     }</conditionExpression>
                 )
               case _ => xml
             }.getOrElse(xml)
           XMergeResult(newElem, warnings)
         }
         } yield result
}
