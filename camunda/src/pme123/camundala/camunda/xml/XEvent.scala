package pme123.camundala.camunda.xml

import scala.xml.Node

sealed trait XEvent
  extends XBpmnNode

case class XStartEvent(xmlNode: Node)
  extends XEvent {
  val tagName = "StartEvent"
}

case class XEndEvent(xmlNode: Node)
  extends XEvent {
  val tagName = "EndEvent"

}
