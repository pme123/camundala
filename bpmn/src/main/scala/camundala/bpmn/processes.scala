package camundala
package bpmn

case class BpmnProcesses(processes: bpmn.Process[?,?]*):

  def :+(process: bpmn.Process[?,?]): BpmnProcesses = BpmnProcesses(
    processes :+ process:_*
  )

object BpmnProcesses:
  def none = new BpmnProcesses()
