package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{ExclusiveGateway, Gateway, ModelException, ParallelGateway, ServiceTask}
import zio.IO

import scala.xml.Elem

sealed trait XGateway[T <: Gateway]
  extends XBpmnNode[T]

case class XExclusiveGateway(xmlElem: Elem)
  extends XGateway[ExclusiveGateway]
    with XHasInFlows[ExclusiveGateway]
    with XHasOutFlows[ExclusiveGateway] {

  val tagName = "ExclusiveGateway"

  def create(): IO[ModelException, ExclusiveGateway] =
    for {
      nodeId <- xBpmnId
      inFlows <- incomingFlows
      outFlows <- outgoingFlows
    } yield
      ExclusiveGateway(
        nodeId,
        inFlows = inFlows,
        outFlows = outFlows
      )
}

case class XParallelGateway(xmlElem: Elem)
  extends XGateway[ParallelGateway]
    with XHasInFlows[ParallelGateway]
    with XHasOutFlows[ParallelGateway] {

  val tagName = "ParallelGateway"

  def create(): IO[ModelException, ParallelGateway] =
    for {
      nodeId <- xBpmnId
      inFlows <- incomingFlows
      outFlows <- outgoingFlows
    } yield
      ParallelGateway(
        nodeId,
        inFlows = inFlows,
        outFlows = outFlows
      )
}


