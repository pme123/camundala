package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{BpmnEvent, EndEvent, ModelException, StartEvent}
import zio.IO

import scala.xml.Elem

sealed trait XEvent[T <: BpmnEvent]
  extends XBpmnNode[T]

case class XStartEvent[T <: StartEvent](xmlElem: Elem)
  extends XEvent[T]
    with XHasForm[T] {
  val tagName = "StartEvent"

  def create(): IO[ModelException, StartEvent] =
    for {
      nodeId <- xBpmnId
    } yield
      StartEvent(
        nodeId
      )
}

case class XEndEvent[T <: EndEvent](xmlElem: Elem)
  extends XEvent[T] {
  val tagName = "EndEvent"

  def create(): IO[ModelException, EndEvent] =
    for {
      nodeId <- xBpmnId
    } yield
      EndEvent(
        nodeId
      )

}
