package pme123.camundala.model.bpmn

case class Bpmn(id: String,
                xml: StaticFile,
                processes: Seq[BpmnProcess],
               ) {
  lazy val processMap: Map[String, BpmnProcess] = processes.map(p => p.id -> p).toMap
  def staticFiles: Set[StaticFile] = processes.flatMap(_.staticFiles).toSet
}

case class BpmnProcess(id: String,
                       userTasks: Seq[UserTask] = Seq.empty,
                       serviceTasks: Seq[ServiceTask] = Seq.empty,
                       sendTasks: Seq[SendTask] = Seq.empty,
                       startEvents: Seq[StartEvent] = Seq.empty,
                       exclusiveGateways: Seq[ExclusiveGateway] = Seq.empty,
                       parallelGateways: Seq[ParallelGateway] = Seq.empty
                      ) {
  def staticFiles: Set[StaticFile] = userTasks.flatMap(_.staticFiles).toSet

  lazy val userTaskMap: Map[String, UserTask] = userTasks.map(t => t.id -> t).toMap
  lazy val serviceTaskMap: Map[String, ServiceTask] = serviceTasks.map(t => t.id -> t).toMap
  lazy val sendTaskMap: Map[String, SendTask] = sendTasks.map(t => t.id -> t).toMap
  lazy val startEventMap: Map[String, StartEvent] = startEvents.map(e => e.id -> e).toMap
  lazy val exclusiveGatewayMap: Map[String, ExclusiveGateway] = exclusiveGateways.map(g => g.id -> g).toMap
  lazy val parallelGatewayMap: Map[String, ParallelGateway] = parallelGateways.map(g => g.id -> g).toMap

}

// org.camunda.bpm.model.bpmn.instance.FlowNode
trait BpmnNode {
  def id: String
}


