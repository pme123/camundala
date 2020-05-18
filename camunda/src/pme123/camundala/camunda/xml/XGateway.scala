package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{ExclusiveGateway, Gateway, ModelException, ParallelGateway, ServiceTask}
import zio.IO

import scala.xml.Elem

sealed trait XGateway[T <: Gateway]
  extends XBpmnNode[T]

case class XExclusiveGateway[T <: ExclusiveGateway](xmlElem: Elem)
  extends XGateway[T] {
  val tagName = "ExclusiveGateway"

  def create(): IO[ModelException, ExclusiveGateway] =
    for{
      nodeId <- xBpmnId
    } yield
      ExclusiveGateway(
        nodeId
      )
}

case class XParallelGateway[T <: ParallelGateway](xmlElem: Elem)
  extends XGateway[T] {
  val tagName = "ParallelGateway"

  def create(): IO[ModelException, ParallelGateway] =
    for{
      nodeId <- xBpmnId
    } yield
      ParallelGateway(
        nodeId
      )
}


