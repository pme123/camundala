package pme123.camundala.camunda.bpmn

import org.camunda.bpm.model.bpmn.instance.InteractionNode
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, instance => camunda}
import pme123.camundala.model.{Bpmn, BpmnProcess, ProcessTask}

import scala.jdk.CollectionConverters._

trait CIdentifiableNode {
  def camNode: InteractionNode

  val id: String = camNode.getId
}

case class CBpmn(model: BpmnModelInstance) {

  val processes: Seq[CBpmnProcess] =
    model.getModelElementsByType(classOf[camunda.Process])
      .asScala.toSeq
      .map(CBpmnProcess)

  def validate(bpmn: Bpmn): ValidateWarnings =
    if (processes.length == bpmn.processes.length)
      processes
        .map(xp => xp.validate(bpmn.processMap.get(xp.id)))
        .foldLeft(ValidateWarnings.none)(_ ++ _)
    else
      ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")

  def merge(bpmn: Bpmn): MergeResult = {
    val processWarnings =
      if (processes.length == bpmn.processes.length)
        processes
          .map(xp => xp.merge(bpmn.processMap.get(xp.id)))
          .map { mr =>
            val removed = model.getDefinitions.removeChildElement(mr.process)
            model.getDefinitions.addChildElement(mr.process)
            mr.warnings
          }
          .foldLeft(ValidateWarnings.none)(_ ++ _)
      else
        ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")
    MergeResult(model, processWarnings)
  }

}


case class CBpmnProcess(process: camunda.Process) {
  val id: String = process.getId
  val userTasks: Seq[CUserTask] =
    process.getChildElementsByType(classOf[camunda.UserTask]).asScala.toSeq.map(CUserTask)

  val serviceTasks: Seq[CServiceTask] =
    process.getChildElementsByType(classOf[camunda.ServiceTask]).asScala.toSeq.map(CServiceTask)

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

  def merge(maybeProcess: Option[BpmnProcess]): MergeProcess =
    maybeProcess match {
      case None =>
        MergeProcess(process, ValidateWarnings(s"There is no Process $id registered"))
      case Some(p) =>
        val valUserTasks = mergeTask(p.userTaskMap, userTasks)
        val valServiceTasks = mergeTask(p.serviceTaskMap, serviceTasks)
        MergeProcess(process, valUserTasks ++ valServiceTasks)
    }


  private def mergeTask(processTaskMap: Map[String, ProcessTask], cTasks: Seq[CProcessTask]) = {
    if (processTaskMap.size == cTasks.length) {
      cTasks
        .map(xt => xt.merge(processTaskMap.get(xt.id)))
        .map { mr =>
          process.removeChildElement(mr.task)
          process.addChildElement(mr.task)
          mr.warnings
        }.foldLeft(ValidateWarnings.none)(_ ++ _)
    } else {
      ValidateWarnings(s"You have ${cTasks.length} UserTasks in the XML-Model, but you have ${cTasks.length} in Scala")
    }
  }
}


