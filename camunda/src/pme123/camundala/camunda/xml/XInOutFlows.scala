package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{HasInFlows, HasOutFlows, ModelException, SequenceFlow, bpmnNodeIdFromStr}
import zio.{IO, ZIO}

import scala.xml.Elem

trait XHasInFlows[T <: HasInFlows] {
  def xmlElem: Elem

  lazy val incomingFlows: IO[ModelException, List[SequenceFlow]] =
    ZIO.foreach(xmlElem \ "incoming") { case e: Elem =>
      bpmnNodeIdFromStr(e.text).map(SequenceFlow(_))
    }
}

trait XHasOutFlows[T <: HasOutFlows] {
  def xmlElem: Elem

  lazy val outgoingFlows: IO[ModelException, List[SequenceFlow]] =
    ZIO.foreach(xmlElem \ "outgoing") { case e: Elem =>
      bpmnNodeIdFromStr(e.text).map(SequenceFlow(_))
    }
}
