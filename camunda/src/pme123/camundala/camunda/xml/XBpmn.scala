package pme123.camundala.camunda.xml

import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.bpmn._
import zio.{Task, UIO, ZIO}

import scala.xml.Elem

trait XIdentifiableNode {
  def xmlElem: Elem

  def tagName: String

  val id: String = xmlElem \@ "id"
}

case class XBpmn(bpmnXml: Elem) {

  val processes: Seq[XBpmnProcess] =
    (bpmnXml \ "process")
      .filter(_ \@ "isExecutable" == "true")
      .map { case e: Elem => XBpmnProcess(e) }

  def merge(bpmn: Bpmn): Task[XMergeResult] = {
    val processWarnings =
      if (processes.length == bpmn.processes.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")
    for {
      mergeResults <- ZIO.foreach(processes)(xp => xp.merge(bpmn.processMap.get(xp.id)))
    } yield
    mergeResults
      .foldLeft(XMergeResult(bpmnXml, processWarnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(procXml, procWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.map(c => if (c \@ "id" != procXml \@ "id") c else procXml)),
            resWarn ++ procWarn)
      }
  }
}

case class XBpmnProcess(xmlElem: Elem)
  extends XIdentifiableNode {
  def tagName: String = "Process"

  val userTasks: Seq[XUserTask[UserTask]] =
    (xmlElem \ "userTask").map { case e: Elem => XUserTask(e) }

  val serviceTasks: Seq[XServiceTask[ServiceTask]] =
    (xmlElem \ "serviceTask").map { case e: Elem => XServiceTask(e) }

  val sendTasks: Seq[XSendTask[SendTask]] =
    (xmlElem \ "sendTask").map { case e: Elem => XSendTask(e) }

  val startEvents: Seq[XStartEvent[StartEvent]] =
    (xmlElem \ "startEvent").map { case e: Elem => XStartEvent(e) }

  val exclusiveGateways: Seq[XExclusiveGateway[ExclusiveGateway]] =
    (xmlElem \ "exclusiveGateway").map { case e: Elem => XExclusiveGateway[ExclusiveGateway](e) }

  val parallelGateways: Seq[XParallelGateway[ParallelGateway]] =
    (xmlElem \ "parallelGateway").map { case e: Elem => XParallelGateway[ParallelGateway](e) }

  val sequenceFlows: Seq[XSequenceFlow[SequenceFlow]] =
    (xmlElem \ "sequenceFlow").map { case e: Elem => XSequenceFlow[SequenceFlow](e) }

  def merge(maybeProcess: Option[BpmnProcess]): Task[XMergeResult] =
    maybeProcess match {
      case None =>
        UIO(XMergeResult(xmlElem, ValidateWarnings(s"There is no Process $id registered")))
      case Some(p) =>
        for {
          XMergeResult(xmlUser, warningsUser) <- mergeExtensionable(xmlElem, p.userTaskMap, userTasks, "UserTask")
          XMergeResult(xmlService, warningsService) <- mergeExtensionable(xmlUser, p.serviceTaskMap, serviceTasks, "Service")
          XMergeResult(xmlStartEvent, warningsStartEvent) <- mergeExtensionable(xmlService, p.startEventMap, startEvents, "StartEvent")
          XMergeResult(xmlExclusiveGateway, warningsExclusiveGateway) <- mergeExtensionable(xmlStartEvent, p.exclusiveGatewayMap, exclusiveGateways, "ExclusiveGateway")
          XMergeResult(xmlParallelGateway, warningsParallelGateway) <- mergeExtensionable(xmlExclusiveGateway, p.parallelGatewayMap, parallelGateways, "ParallelGateway")
          XMergeResult(xmlSequenceFlow, warningsSequenceFlow) <- mergeExtensionable(xmlParallelGateway, p.sequenceFlowMap, sequenceFlows, "SequenceFlow")
        } yield
          XMergeResult(xmlSequenceFlow, warningsUser ++ warningsService ++ warningsStartEvent ++ warningsExclusiveGateway ++ warningsParallelGateway ++ warningsSequenceFlow)
    }


  private def mergeExtensionable[A <: Extensionable](xml: Elem, extensionableMap: Map[String, A], xExts: Seq[XBpmnNode[A]], label: String): Task[XMergeResult] = {
    val warnings =
      if (extensionableMap.size == xExts.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${xExts.length} $label in the XML-Model, but you have ${extensionableMap.size} in Scala")

    for {
      mergeResults <- ZIO.foreach(xExts)(xt => xt.merge(extensionableMap.get(xt.id)))
    } yield
      mergeResults.foldLeft(XMergeResult(xml, warnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(xml, taskWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.filter(c => c \@ "id" != xml \@ "id") :+ xml),
            resWarn ++ taskWarn)
      }
  }
}

trait XBpmnNode[T <: Extensionable]
  extends XIdentifiableNode {

  def merge(maybeNode: Option[T]): Task[XMergeResult] = Task((maybeNode, xmlElem) match {
    case (None, _) =>
      XMergeResult(xmlElem, ValidateWarnings(s"There is NOT a $tagName with id '$id' in Scala."))
    case (Some(extensionable), nodeElem: Elem) =>
      val propElem = nodeElem \\ "property"
      val propParams = propElem.map(_ \@ "name")
      val child = nodeElem.child
      val otherChild = child.filter(_.label != "extensionElements")
      val xmlElem: Elem =
        nodeElem.copy(
          child = <extensionElements xmlns:camunda={xmlnsCamunda} xmlns={xmlnsBpmn}>
            <camunda:properties>
              {for {(k, v) <- extensionable.extensions.properties
                    if !propParams.contains(k) // only add the one that not exist
                    } yield
                <camunda:property name={k} value={v}/>}{//
              propElem}
            </camunda:properties>
          </extensionElements>
          ++ otherChild
        )
      XMergeResult(xmlElem, ValidateWarnings.none)
    case (Some(_), _) =>
      XMergeResult(xmlElem, ValidateWarnings(s"The XML Node must be a XML Elem not just a Node ($tagName with id '$id')."))
  })
}
