package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{BpmnEvent, EndEvent, ModelException, SequenceFlow, StartEvent}
import zio.IO

import scala.xml.Elem

sealed trait XEvent[T <: BpmnEvent]
  extends XBpmnNode[T]

case class XStartEvent(xmlElem: Elem)
  extends XEvent[StartEvent]
    with XHasForm[StartEvent]
    with XHasOutFlows[StartEvent] {

  val tagName = "StartEvent"

  def create(): IO[ModelException, StartEvent] =
    for {
      nodeId <- xBpmnId
      outFlows <- outgoingFlows
    } yield
      StartEvent(
        nodeId,
        outFlows = outFlows
      )
}

case class XEndEvent(xmlElem: Elem)
  extends XEvent[EndEvent]
    with XHasInFlows[EndEvent] {
  val tagName = "EndEvent"

  def create(): IO[ModelException, EndEvent] =
    for {
      nodeId <- xBpmnId
      inFlows <- incomingFlows
    } yield
      EndEvent(
        nodeId,
        inFlows = inFlows
      )

}
