package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn._
import zio.{IO, ZIO}

import scala.xml.{Elem, NodeSeq}

trait XHasInOutFlows {
  protected def zForeach(
      nodeSeq: NodeSeq
  )(funct: Elem => IO[ModelException, SequenceFlow]) = {
    ZIO.foreach(Seq(nodeSeq: _*)) { case e: Elem => funct(e) }
  }
}

trait XHasInFlows[T <: HasInFlows] extends XHasInOutFlows {
  def xmlElem: Elem

  lazy val incomingFlows: IO[ModelException, Seq[SequenceFlow]] =
    zForeach(xmlElem \ "incoming") { e =>
      bpmnNodeIdFromStr(e.text).map(SequenceFlow(_))
    }
}

trait XHasOutFlows[T <: HasOutFlows] extends XHasInOutFlows {
  def xmlElem: Elem

  lazy val outgoingFlows: IO[ModelException, Seq[SequenceFlow]] =
    zForeach(xmlElem \ "outcoming") { e =>
      bpmnNodeIdFromStr(e.text).map(SequenceFlow(_))
    }
}
