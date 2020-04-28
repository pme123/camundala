package pme123.camundala.model


case class Bpmn(id: String,
                processes: Seq[BpmnProcess],
                staticFiles: Set[StaticFile]) {
  lazy val processMap: Map[String, BpmnProcess] = processes.map(p => p.id -> p).toMap
}

case class BpmnProcess(id: String,
                       userTasks: Seq[UserTask],
                       serviceTasks: Seq[ServiceTask],
                       startEvents: Seq[StartEvent],
                       gateways: Seq[Gateway]
                      ) {
  lazy val userTaskMap: Map[String, UserTask] = userTasks.map(t => t.id -> t).toMap
  lazy val serviceTaskMap: Map[String, ServiceTask] = serviceTasks.map(t => t.id -> t).toMap
  lazy val startEventMap: Map[String, StartEvent] = startEvents.map(e => e.id -> e).toMap
  lazy val gatewayMap: Map[String, Gateway] = gateways.map(g => g.id -> g).toMap

}

// org.camunda.bpm.model.bpmn.instance.FlowNode
trait BpmnNode {
  def id: String
}


