package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{BpmnEvent, EndEvent, StartEvent}

import scala.xml.Elem

sealed trait XEvent[T <: BpmnEvent]
  extends XBpmnNode[T]

case class XStartEvent[T <: StartEvent](xmlElem: Elem)
  extends XEvent[T]
    with XHasForm[T] {
  val tagName = "StartEvent"
}

case class XEndEvent[T <: EndEvent](xmlElem: Elem)
  extends XEvent[T] {
  val tagName = "EndEvent"

}
