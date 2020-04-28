package pme123.camundala.camunda.xml

import scala.xml.Node

sealed trait XProcessTask
  extends XBpmnNode

case class XServiceTask(xmlNode: Node)
  extends XProcessTask {
  val tagName = "ServiceTask"
}

case class XUserTask(xmlNode: Node)
  extends XProcessTask {
  val tagName = "UserTask"

}
