package pme123.camundala.camunda.xml

import pme123.camundala.camunda.bpmn.ValidateWarnings
import pme123.camundala.model.{Bpmn, BpmnProcess, ProcessTask}

import scala.xml.{Elem, Node}

trait XIdentifiableNode {
  def xmlNode: Node

  val xmlnsBpmn = "http://www.omg.org/spec/BPMN/20100524/MODEL"
  val xmlnsCamunda = "http://camunda.org/schema/1.0/bpmn"

  val id: String = xmlNode \@ "id"
}

case class XBpmn(bpmnXml: Elem) {

  val processes: Seq[XBpmnProcess] =
    (bpmnXml \ "process")
      .filter(_ \@ "isExecutable" == "true")
      .map(XBpmnProcess)

  def validate(bpmn: Bpmn): ValidateWarnings =
    if (processes.length == bpmn.processes.length)
      processes
        .map(xp => xp.validate(bpmn.processMap.get(xp.id)))
        .foldLeft(ValidateWarnings.none)(_ ++ _)
    else
      ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")

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
          XMergeResult(resXml.copy(child = resXml.child.map(c => if(c \@ "id" != procXml \@ "id") c else procXml)),
            resWarn ++ procWarn)
      }
  }
}

case class XBpmnProcess(xmlNode: Node)
  extends XIdentifiableNode {

  val userTasks: Seq[XUserTask] =
    (xmlNode \ "userTask").map(XUserTask)

  val serviceTasks: Seq[XServiceTask] =
    (xmlNode \ "serviceTask").map(XServiceTask)

  def validate(maybeProcess: Option[BpmnProcess]): ValidateWarnings = maybeProcess match {
    case None =>
      ValidateWarnings(s"There is no Process $id registered")
    case Some(p) =>
      val valUserTasks =
        if (p.userTasks.length == userTasks.length) {
          userTasks
            .map(xt => xt.validate(p.userTaskMap.get(xt.id)))
            .foldLeft(ValidateWarnings.none)(_ ++ _)
        } else {
          ValidateWarnings(s"You have ${userTasks.length} UserTasks in the XML-Model, but you have ${p.userTasks.length} in Scala")
        }
      val valServiceTasks =
        if (p.serviceTaskMap.size == serviceTasks.length) {
          serviceTasks
            .map(xt => xt.validate(p.serviceTaskMap.get(xt.id)))
            .foldLeft(ValidateWarnings.none)(_ ++ _)
        } else {
          ValidateWarnings(s"You have ${serviceTasks.length} ServiceTasks in the XML-Model, but you have ${p.serviceTaskMap.size} in Scala")
        }
      valUserTasks ++ valServiceTasks
  }

  def merge(maybeProcess: Option[BpmnProcess]): XMergeResult =
    maybeProcess match {
      case None =>
        XMergeResult(xmlNode, ValidateWarnings(s"There is no Process $id registered"))
      case Some(p) =>
        val XMergeResult(xmlUser, warningsUser) = mergeTask(xmlNode, p.userTaskMap, userTasks)
        val XMergeResult(xmlService, warningsService) = mergeTask(xmlUser, p.serviceTaskMap, serviceTasks)
        XMergeResult(xmlService, warningsUser ++ warningsService)
    }


  private def mergeTask(xml: Node, processTaskMap: Map[String, ProcessTask], xTasks: Seq[XProcessTask]) = {
    val taskWarnings =
      if (processTaskMap.size == xTasks.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${xTasks.length} UserTasks in the XML-Model, but you have ${xTasks.length} in Scala")

    xTasks
      .map(xt => xt.merge(processTaskMap.get(xt.id)))
      .foldLeft(XMergeResult(xml, taskWarnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(taskXml, taskWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.filter(c => c \@ "id" != taskXml \@ "id") :+ taskXml),
            resWarn ++ taskWarn)
      }
  }
}


