package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{BpmnEvent, EndEvent, SequenceFlow, StartEvent}

import scala.xml.Elem

case class XSequenceFlow[T <: SequenceFlow](xmlElem: Elem)
  extends XBpmnNode[T] {
  val tagName = "SequenceFlow"
}
