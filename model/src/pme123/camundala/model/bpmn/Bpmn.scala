package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

case class Bpmn(id: BpmnId,
                xml: StaticFile,
                processes: Seq[BpmnProcess] = Seq.empty,
               ) {

  def groups(): Seq[Group] = processes.flatMap(_.groups())

  def users(): Seq[User] = processes.flatMap(_.users())

  def ###(proc: BpmnProcess): Bpmn = process(proc)

  def process(proc: BpmnProcess): Bpmn = copy(processes = processes :+ proc)

  def generate(): String =
    s"""
       |Bpmn("$id",
       |  ${xml.generate()},
       |  List(${processes.map(_.generate()).mkString(",")}))
       |""".stripMargin

  lazy val processMap: Map[BpmnId, BpmnProcess] = processes.map(p => p.id -> p).toMap

  def staticFiles: Set[StaticFile] = processes.flatMap(_.staticFiles).toSet
}

object Bpmn {
  private val DefaultBpmnDirectory: PathElem = "bpmn"

  def apply(id: BpmnId, xmlName: FilePath): Bpmn = new Bpmn(id, StaticFile(xmlName, DefaultBpmnDirectory))
}

case class BpmnProcess(id: ProcessId,
                       starterGroups: CandidateGroups = CandidateGroups.none,
                       starterUsers: CandidateUsers = CandidateUsers.none,
                       userTasks: Seq[UserTask] = Seq.empty,
                       serviceTasks: Seq[ServiceTask] = Seq.empty,
                       sendTasks: Seq[SendTask] = Seq.empty,
                       startEvents: Seq[StartEvent] = Seq.empty,
                       exclusiveGateways: Seq[ExclusiveGateway] = Seq.empty,
                       parallelGateways: Seq[ParallelGateway] = Seq.empty,
                       sequenceFlows: Seq[SequenceFlow] = Seq.empty,
                      ) {

  def groups(): Seq[Group] = userTasks.flatMap(_.groups()) ++ starterGroups.groups

  def users(): Seq[User] = userTasks.flatMap(_.users()) ++ starterUsers.users

  def generate(): String =
    s"""
       |      BpmnProcess("${id.value}",
       |            userTasks = List(${userTasks.map(_.generate()).mkString(",")}),
       |            serviceTasks = List(${serviceTasks.map(_.generate()).mkString(",")}),
       |            sendTasks = List(${sendTasks.map(_.generate()).mkString(",")}),
       |            startEvents = List(${startEvents.map(_.generate()).mkString(",")}),
       |            exclusiveGateways = List(${exclusiveGateways.map(_.generate()).mkString(",")}),
       |            parallelGateways = List(${parallelGateways.map(_.generate()).mkString(",")}),
       |            sequenceFlows = List(${sequenceFlows.map(_.generate()).mkString(",")}),
       |)""".stripMargin

  def staticFiles: Set[StaticFile] =
    userTasks.flatMap(_.staticFiles).toSet ++
      startEvents.flatMap(_.staticFiles)

  lazy val userTaskMap: Map[BpmnNodeId, UserTask] = userTasks.map(t => t.id -> t).toMap
  lazy val serviceTaskMap: Map[BpmnNodeId, ServiceTask] = serviceTasks.map(t => t.id -> t).toMap
  lazy val sendTaskMap: Map[BpmnNodeId, SendTask] = sendTasks.map(t => t.id -> t).toMap
  lazy val startEventMap: Map[BpmnNodeId, StartEvent] = startEvents.map(e => e.id -> e).toMap
  lazy val exclusiveGatewayMap: Map[BpmnNodeId, ExclusiveGateway] = exclusiveGateways.map(g => g.id -> g).toMap
  lazy val parallelGatewayMap: Map[BpmnNodeId, ParallelGateway] = parallelGateways.map(g => g.id -> g).toMap
  lazy val sequenceFlowMap: Map[BpmnNodeId, SequenceFlow] = sequenceFlows.map(g => g.id -> g).toMap

  def starterGroup(group: Group): BpmnProcess = copy(starterGroups = starterGroups :+ group)

  def starterUser(user: User): BpmnProcess = copy(starterUsers = starterUsers :+ user)

  def ***(userTask: UserTask): BpmnProcess = copy(userTasks = userTasks :+ userTask)

  def ***(serviceTask: ServiceTask): BpmnProcess = copy(serviceTasks = serviceTasks :+ serviceTask)

  def sendTask(sendTask: SendTask): BpmnProcess = copy(sendTasks = sendTasks :+ sendTask)

  def ***(startEvent: StartEvent): BpmnProcess = copy(startEvents = startEvents :+ startEvent)

  def ***(exclusiveGateway: ExclusiveGateway): BpmnProcess = copy(exclusiveGateways = exclusiveGateways :+ exclusiveGateway)

  def parallelGateway(parallelGateway: ParallelGateway): BpmnProcess = copy(parallelGateways = parallelGateways :+ parallelGateway)

  def sequenceFlow(sequenceFlow: SequenceFlow): BpmnProcess = copy(sequenceFlows = sequenceFlows :+ sequenceFlow)

}

// org.camunda.bpm.model.bpmn.instance.FlowNode
trait BpmnNode {
  def id: BpmnNodeId

  def generate(): String =
    s"""${getClass.getSimpleName}("${id.value}")"""
}


