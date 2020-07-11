package pme123.camundala.model.bpmn

case class Bpmn(id: BpmnId,
                xml: StaticFile,
                processes: Seq[BpmnProcess] = Seq.empty,
               ) {

  def groups(): Seq[Group] = processes.flatMap(_.groups())

  def users(): Seq[User] = processes.flatMap(_.users())

  def processes(procs: BpmnProcess*): Bpmn = copy(processes = processes ++ procs)

  def process(proc: BpmnProcess): Bpmn = processes(proc)

  def ###(proc: BpmnProcess): Bpmn = processes(proc)

  lazy val idVal: String = idAsVal(id.value)

  def generateDsl(): String =
    s"""
       |lazy val $idVal: Bpmn =
       |  Bpmn("$id", ${xml.generateDsl()})
       |    .processes(
       |       ${processes.map(p => s"${p.idVal}").mkString(",\n")}
       |    )
       |
       |${processes.map(_.generateDsl()).mkString("\n")}
       |""".stripMargin

  lazy val processMap: Map[BpmnId, BpmnProcess] = processes.map(p => p.id -> p).toMap

  def staticFiles: Set[StaticFile] = processes.flatMap(_.staticFiles).toSet
}

object Bpmn {

  def apply(id: BpmnId, xmlName: FilePath): Bpmn = new Bpmn(id, StaticFile(xmlName))
}

case class BpmnProcess(id: ProcessId,
                       starterGroups: CandidateGroups = CandidateGroups.none,
                       starterUsers: CandidateUsers = CandidateUsers.none,
                       userTasks: Seq[UserTask] = Seq.empty,
                       serviceTasks: Seq[ServiceTask] = Seq.empty,
                       businessRuleTasks: Seq[BusinessRuleTask] = Seq.empty,
                       sendTasks: Seq[SendTask] = Seq.empty,
                       callActivities: Seq[CallActivity] = Seq.empty,
                       startEvents: Seq[StartEvent] = Seq.empty,
                       endEvents: Seq[EndEvent] = Seq.empty,
                       exclusiveGateways: Seq[ExclusiveGateway] = Seq.empty,
                       parallelGateways: Seq[ParallelGateway] = Seq.empty,
                       sequenceFlows: Seq[SequenceFlow] = Seq.empty,
                      ) {

  def groups(): Seq[Group] = userTasks.flatMap(_.groups()) ++ starterGroups.groups

  def users(): Seq[User] = userTasks.flatMap(_.users()) ++ starterUsers.users

  /**
    * create an ordered list of all nodes, grouped by there names
    */
  def allNodes: Seq[(String, Seq[BpmnNode])] =
    Seq(("startEvents", startEvents),
      ("userTasks", userTasks),
      ("serviceTasks", serviceTasks),
      ("businessRuleTasks", businessRuleTasks),
      ("sendTasks", sendTasks),
      ("callActivities", callActivities),
      ("exclusiveGateways", exclusiveGateways),
      ("parallelGateways", parallelGateways),
      ("endEvents", endEvents),
      ("sequenceFlows", sequenceFlows)
    )

  lazy val idVal: String = idAsVal(id.value)

  def generateDsl(): String =
    s"""//### $idVal ###
       |lazy val $idVal: BpmnProcess =
       |  BpmnProcess("$id")
       |${
      allNodes.map { case (name: String, nodes: Seq[BpmnNode]) =>
        s"""    .$name(
           |${nodes.map(n => s"        ${n.idVal}").mkString(",\n")}
           |    )""".stripMargin
      }.mkString("\n")
    }
       |
       |${
      allNodes.map { case (name: String, nodes: Seq[BpmnNode]) =>
        s"""//*** $name ***
           |${nodes.map(_.generateDsl()).mkString("\n")}""".stripMargin
      }.mkString("\n")
    }
       |""".stripMargin

  def staticFiles: Set[StaticFile] =
    startEvents.flatMap(_.formStaticFiles).toSet ++
      userTasks.flatMap(_.formStaticFiles) ++
      userTasks.flatMap(_.inOutStaticFiles) ++
      serviceTasks.flatMap(_.inOutStaticFiles) ++
      serviceTasks.flatMap(_.implStaticFiles) ++
      sendTasks.flatMap(_.inOutStaticFiles) ++
      sendTasks.flatMap(_.implStaticFiles) ++
      businessRuleTasks.flatMap(_.implStaticFiles) ++
      businessRuleTasks.flatMap(_.inOutStaticFiles)

  lazy val userTaskMap: Map[BpmnNodeId, UserTask] = userTasks.map(t => t.id -> t).toMap
  lazy val serviceTaskMap: Map[BpmnNodeId, ServiceTask] = serviceTasks.map(t => t.id -> t).toMap
  lazy val businessRuleTaskMap: Map[BpmnNodeId, BusinessRuleTask] = businessRuleTasks.map(t => t.id -> t).toMap
  lazy val sendTaskMap: Map[BpmnNodeId, SendTask] = sendTasks.map(t => t.id -> t).toMap
  lazy val startEventMap: Map[BpmnNodeId, StartEvent] = startEvents.map(e => e.id -> e).toMap
  lazy val exclusiveGatewayMap: Map[BpmnNodeId, ExclusiveGateway] = exclusiveGateways.map(g => g.id -> g).toMap
  lazy val parallelGatewayMap: Map[BpmnNodeId, ParallelGateway] = parallelGateways.map(g => g.id -> g).toMap
  lazy val sequenceFlowMap: Map[BpmnNodeId, SequenceFlow] = sequenceFlows.map(g => g.id -> g).toMap

  def starterGroups(groups: Group*): BpmnProcess = copy(starterGroups = starterGroups ++ groups)

  def starterGroup(group: Group): BpmnProcess = starterGroups(group)

  def ***(group: Group): BpmnProcess = starterGroup(group)

  def starterUsers(users: User*): BpmnProcess = copy(starterUsers = starterUsers ++ users)

  def starterUser(user: User): BpmnProcess = starterUsers(user)

  def ***(user: User): BpmnProcess = starterUser(user)

  def startEvents(events: StartEvent*): BpmnProcess = copy(startEvents = startEvents ++ events)

  def startEvent(event: StartEvent): BpmnProcess = startEvents(event)

  def ***(event: StartEvent): BpmnProcess = startEvents(event)

  def userTasks(tasks: UserTask*): BpmnProcess = copy(userTasks = userTasks ++ tasks)

  def userTask(task: UserTask): BpmnProcess = userTasks(task)

  def ***(task: UserTask): BpmnProcess = userTasks(task)

  def serviceTasks(tasks: ServiceTask*): BpmnProcess = copy(serviceTasks = serviceTasks ++ tasks)

  def serviceTask(task: ServiceTask): BpmnProcess = serviceTasks(task)

  def ***(task: ServiceTask): BpmnProcess = serviceTasks(task)

  def businessRuleTasks(tasks: BusinessRuleTask*): BpmnProcess = copy(businessRuleTasks = businessRuleTasks ++ tasks)

  def businessRuleTask(task: BusinessRuleTask): BpmnProcess = businessRuleTasks(task)

  def ***(task: BusinessRuleTask): BpmnProcess = businessRuleTasks(task)

  def callActivities(activities: CallActivity*): BpmnProcess = copy(callActivities = callActivities ++ activities)

  def callActivity(activity: CallActivity): BpmnProcess = callActivities(activity)

  def ***(activity: CallActivity): BpmnProcess = callActivities(activity)

  def sendTasks(tasks: SendTask*): BpmnProcess = copy(sendTasks = sendTasks ++ tasks)

  def sendTask(task: SendTask): BpmnProcess = sendTasks(task)

  def ***(task: SendTask): BpmnProcess = sendTasks(task)

  def exclusiveGateways(gateways: ExclusiveGateway*): BpmnProcess = copy(exclusiveGateways = exclusiveGateways ++ gateways)

  def exclusiveGateway(gateway: ExclusiveGateway): BpmnProcess = exclusiveGateways(gateway)

  def ***(gateway: ExclusiveGateway): BpmnProcess = exclusiveGateways(gateway)

  def parallelGateways(gateways: ParallelGateway*): BpmnProcess = copy(parallelGateways = parallelGateways ++ gateways)

  def parallelGateway(gateway: ParallelGateway): BpmnProcess = parallelGateways(gateway)

  def ***(gateway: ParallelGateway): BpmnProcess = parallelGateways(gateway)

  def sequenceFlows(flows: SequenceFlow*): BpmnProcess = copy(sequenceFlows = sequenceFlows ++ flows)

  def sequenceFlow(flow: SequenceFlow): BpmnProcess = sequenceFlows(flow)

  def ***(flow: SequenceFlow): BpmnProcess = sequenceFlows(flow)

}

// org.camunda.bpm.model.bpmn.instance.FlowNode
trait BpmnNode {
  self =>
  def id: BpmnNodeId

  lazy val idVal: String = idAsVal(id.value)

  def generate(): String =
    s"""${getClass.getSimpleName}("$id")"""

  def generateDsl(): String =
    s"""lazy val $idVal: ${getClass.getSimpleName} = ${generate()}""".stripMargin +
      (self match {
        case n: HasInFlows =>
          n.generateInFlowDsl
        case _ => ""
      }) +
      (self match {
        case n: HasOutFlows =>
          n.generateOutFlowDsl
        case _ => ""
      })
}


