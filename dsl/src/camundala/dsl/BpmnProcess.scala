package camundala.dsl

case class BpmnProcess(id: Identifier,
                       starterGroups: CandidateGroups = CandidateGroups.none,
                       starterUsers: CandidateUsers = CandidateUsers.none,
                       startEvents: Seq[StartEvent] = Seq.empty,
                       userTasks: Seq[UserTask] = Seq.empty,
                       serviceTasks: Seq[ServiceTask] = Seq.empty,
                       businessRuleTasks: Seq[BusinessRuleTask] = Seq.empty,
                       sendTasks: Seq[SendTask] = Seq.empty,
                       callActivities: Seq[CallActivity] = Seq.empty,
                       endEvents: Seq[EndEvent] = Seq.empty,
                       sequenceFlows: Seq[SequenceFlow] = Seq.empty,
                       exclusiveGateways: Seq[ExclusiveGateway] = Seq.empty,
                       parallelGateways: Seq[ParallelGateway] = Seq.empty,
                      ) {

  def canStart(group: BpmnGroup, groups: BpmnGroup*): BpmnProcess = copy(starterGroups = (starterGroups :+ group) ++ groups)

  def canStart(user: BpmnUser, users: BpmnUser*): BpmnProcess = copy(starterUsers = (starterUsers :+ user) ++ users)

  def userTasks(task: UserTask, tasks: UserTask*): BpmnProcess = copy(userTasks = (userTasks :+ task) ++ tasks)

  def ---(task: UserTask, tasks: UserTask*): BpmnProcess = userTasks(task, tasks: _*)

  def serviceTasks(task: ServiceTask, tasks: ServiceTask*): BpmnProcess = copy(serviceTasks = (serviceTasks :+ task) ++ tasks)

  def ---(task: ServiceTask, tasks: ServiceTask*): BpmnProcess = serviceTasks(task, tasks: _*)

  def sendTasks(task: SendTask, tasks: SendTask*): BpmnProcess = copy(sendTasks = (sendTasks :+ task) ++ tasks)

  def ---(task: SendTask, tasks: SendTask*): BpmnProcess = sendTasks(task, tasks: _*)

  def businessRuleTasks(task: BusinessRuleTask, tasks: BusinessRuleTask*): BpmnProcess = copy(businessRuleTasks = (businessRuleTasks :+ task) ++ tasks)

  def ---(task: BusinessRuleTask, tasks: BusinessRuleTask*): BpmnProcess = businessRuleTasks(task, tasks: _*)

  def callActivities(activity: CallActivity, activities: CallActivity*): BpmnProcess = copy(callActivities = (callActivities :+ activity) ++ activities)

  def ---(activity: CallActivity, activities: CallActivity*): BpmnProcess = callActivities(activity, activities: _*)

  def starts(event: StartEvent, events: StartEvent*): BpmnProcess = copy(startEvents = (startEvents :+ event) ++ events)

  def ---(event: StartEvent, events: StartEvent*): BpmnProcess = starts(event, events: _*)

  def ends(event: EndEvent, events: EndEvent*): BpmnProcess = copy(endEvents = (endEvents :+ event) ++ events)

  def ---(event: EndEvent, events: EndEvent*): BpmnProcess = ends(event, events: _*)

  def sequenceFlows(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess = copy(callActivities = (sequenceFlows :+ flow) ++ flows)

  def ---(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess = sequenceFlows(flow, flows: _*)

}
