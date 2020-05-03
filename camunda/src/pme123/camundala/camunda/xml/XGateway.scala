package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{ExclusiveGateway, Gateway, ParallelGateway}

import scala.xml.Elem

sealed trait XGateway[T <: Gateway]
  extends XBpmnNode[T]

case class XExclusiveGateway[T <: ExclusiveGateway](xmlElem: Elem)
  extends XGateway[T] {
  val tagName = "ExclusiveGateway"
}

case class XParallelGateway[T <: ParallelGateway](xmlElem: Elem)
  extends XGateway[T] {
  val tagName = "ParallelGateway"
}


