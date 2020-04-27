package pme123.camundala.model

trait Identifiable {
  def id: String
}

case class Bpmn(processes: Seq[BpmnProcess]) {
  lazy val processMap: Map[String, BpmnProcess] = processes.map(p => p.id -> p).toMap
}

case class BpmnProcess(id: String,
                       userTasks: Seq[UserTask],
                       serviceTasks: Seq[ServiceTask]
                      ) extends Identifiable {
  lazy val userTaskMap: Map[String, UserTask] = userTasks.map(t => t.id -> t).toMap
  lazy val serviceTaskMap: Map[String, ServiceTask] = serviceTasks.map(t => t.id -> t).toMap

}


