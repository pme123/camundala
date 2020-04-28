package pme123.camundala.camunda.xml

import scala.xml.Node

sealed trait XGateway
  extends XBpmnNode

case class XExclusiveGateway(xmlNode: Node)
  extends XGateway {
  val tagName = "ExclusiveGateway"
}


