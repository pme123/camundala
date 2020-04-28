package pme123.camundala.camunda.xml

import pme123.camundala.model.{Bpmn, BpmnProcess, Extensionable}

import scala.xml.{Elem, Node}

trait XIdentifiableNode {
  def xmlNode: Node

  def tagName: String

  val xmlnsBpmn = "http://www.omg.org/spec/BPMN/20100524/MODEL"
  val xmlnsCamunda = "http://camunda.org/schema/1.0/bpmn"

  val id: String = xmlNode \@ "id"
}

case class XBpmn(bpmnXml: Elem) {

  val processes: Seq[XBpmnProcess] =
    (bpmnXml \ "process")
      .filter(_ \@ "isExecutable" == "true")
      .map(XBpmnProcess)

  def merge(bpmn: Bpmn): XMergeResult = {
    val processWarnings =
      if (processes.length == bpmn.processes.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")

    processes
      .map(xp => xp.merge(bpmn.processMap.get(xp.id)))
      .foldLeft(XMergeResult(bpmnXml, processWarnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(procXml, procWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.map(c => if (c \@ "id" != procXml \@ "id") c else procXml)),
            resWarn ++ procWarn)
      }
  }
}

case class XBpmnProcess(xmlNode: Node)
  extends XIdentifiableNode {
  def tagName: String = "Process"

  val userTasks: Seq[XUserTask] =
    (xmlNode \ "userTask").map(XUserTask)

  val serviceTasks: Seq[XServiceTask] =
    (xmlNode \ "serviceTask").map(XServiceTask)

  val startEvents: Seq[XStartEvent] =
    (xmlNode \ "startEvent").map(XStartEvent)

  val exclusiveGateways: Seq[XExclusiveGateway] =
    (xmlNode \ "exclusiveGateway").map(XExclusiveGateway)

  def merge(maybeProcess: Option[BpmnProcess]): XMergeResult =
    maybeProcess match {
      case None =>
        XMergeResult(xmlNode, ValidateWarnings(s"There is no Process $id registered"))
      case Some(p) =>
        val XMergeResult(xmlUser, warningsUser) = mergeExtensionable(xmlNode, p.userTaskMap, userTasks, "UserTask")
        val XMergeResult(xmlService, warningsService) = mergeExtensionable(xmlUser, p.serviceTaskMap, serviceTasks, "Service")
        val XMergeResult(xmlStartEvent, warningsStartEvent) = mergeExtensionable(xmlService, p.startEventMap, startEvents, "StartEvent")
        val XMergeResult(xmlGateway, warningsGateway) = mergeExtensionable(xmlStartEvent, p.gatewayMap, exclusiveGateways, "ExclusiveGateway")
        XMergeResult(xmlGateway, warningsUser ++ warningsService ++ warningsStartEvent ++ warningsGateway)
    }


  private def mergeExtensionable(xml: Node, extensionableMap: Map[String, Extensionable], xExts: Seq[XBpmnNode], label:String) = {
    val warnings =
      if (extensionableMap.size == xExts.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${xExts.length} $label in the XML-Model, but you have ${extensionableMap.size} in Scala")

    xExts
      .map(xt => xt.merge(extensionableMap.get(xt.id)))
      .foldLeft(XMergeResult(xml, warnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(xml, taskWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.filter(c => c \@ "id" != xml \@ "id") :+ xml),
            resWarn ++ taskWarn)
      }
  }
}

trait XBpmnNode
  extends XIdentifiableNode {

  def merge(maybeNode: Option[Extensionable]): XMergeResult = (maybeNode, xmlNode) match {
    case (None, _) =>
      XMergeResult(xmlNode, ValidateWarnings(s"There is NOT a $tagName with id '$id' in Scala."))
    case (Some(extensionable), nodeElem: Elem) =>
      val propElem = nodeElem \\ "property"
      val propParams = propElem.map(_ \@ "name")
      val xmlElem: Elem = nodeElem.copy(
        child = <extensionElements xmlns:camunda={xmlnsCamunda} xmlns={xmlnsBpmn}>
          <camunda:properties>
            {for {(k, v) <- extensionable.extensions.properties
                  if !propParams.contains(k) // only add the one that not exist
                  } yield
              <camunda:property name={k} value={v}/>}{//
            propElem}
          </camunda:properties>
        </extensionElements>
      )
      XMergeResult(xmlElem, ValidateWarnings.none)
  }
}
