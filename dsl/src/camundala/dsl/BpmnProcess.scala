package camundala.dsl

import camundala.dsl.BpmnProcess.NodeKey
import eu.timepit.refined.auto._

case class BpmnProcess(
    id: Identifier,
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
    parallelGateways: Seq[ParallelGateway] = Seq.empty
)  extends IdentifiableNode {

  def canStart(group: BpmnGroup, groups: BpmnGroup*): BpmnProcess =
    copy(starterGroups = (starterGroups :+ group) ++ groups)

  def canStart(user: BpmnUser, users: BpmnUser*): BpmnProcess =
    copy(starterUsers = (starterUsers :+ user) ++ users)

  def userTasks(task: UserTask, tasks: UserTask*): BpmnProcess =
    copy(userTasks = (userTasks :+ task) ++ tasks)

  def ---(task: UserTask, tasks: UserTask*): BpmnProcess =
    userTasks(task, tasks: _*)

  def serviceTasks(task: ServiceTask, tasks: ServiceTask*): BpmnProcess =
    copy(serviceTasks = (serviceTasks :+ task) ++ tasks)

  def ---(task: ServiceTask, tasks: ServiceTask*): BpmnProcess =
    serviceTasks(task, tasks: _*)

  def sendTasks(task: SendTask, tasks: SendTask*): BpmnProcess =
    copy(sendTasks = (sendTasks :+ task) ++ tasks)

  def ---(task: SendTask, tasks: SendTask*): BpmnProcess =
    sendTasks(task, tasks: _*)

  def businessRuleTasks(
      task: BusinessRuleTask,
      tasks: BusinessRuleTask*
  ): BpmnProcess =
    copy(businessRuleTasks = (businessRuleTasks :+ task) ++ tasks)

  def ---(task: BusinessRuleTask, tasks: BusinessRuleTask*): BpmnProcess =
    businessRuleTasks(task, tasks: _*)

  def callActivities(
      activity: CallActivity,
      activities: CallActivity*
  ): BpmnProcess =
    copy(callActivities = (callActivities :+ activity) ++ activities)

  def ---(activity: CallActivity, activities: CallActivity*): BpmnProcess =
    callActivities(activity, activities: _*)

  def startEvents(event: StartEvent, events: StartEvent*): BpmnProcess =
    copy(startEvents = (startEvents :+ event) ++ events)

  def ---(event: StartEvent, events: StartEvent*): BpmnProcess =
    startEvents(event, events: _*)

  def endEvents(event: EndEvent, events: EndEvent*): BpmnProcess =
    copy(endEvents = (endEvents :+ event) ++ events)

  def ---(event: EndEvent, events: EndEvent*): BpmnProcess =
    endEvents(event, events: _*)

  def sequenceFlows(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess =
    copy(sequenceFlows = (sequenceFlows :+ flow) ++ flows)

  def ---(flow: SequenceFlow, flows: SequenceFlow*): BpmnProcess =
    sequenceFlows(flow, flows: _*)

  /**
    * create an ordered list of all nodes, grouped by there names
    */
  val allNodes: Seq[(NodeKey, Seq[IdentifiableNode])] =
    Seq(
      (NodeKey.startEvents, startEvents),
      (NodeKey.userTasks, userTasks),
      (NodeKey.serviceTasks, serviceTasks),
      (NodeKey.businessRuleTasks, businessRuleTasks),
      (NodeKey.sendTasks, sendTasks),
      (NodeKey.callActivities, callActivities),
      (NodeKey.exclusiveGateways, exclusiveGateways),
      (NodeKey.parallelGateways, parallelGateways),
      (NodeKey.endEvents, endEvents),
      (NodeKey.sequenceFlows, sequenceFlows)
    )

}

object BpmnProcess {
  private val process =
    BpmnProcess("dummy")
  val allNodeKeys: Seq[NodeKey] =
    process.allNodes.map(_._1)
  val emptyAllNodes: Map[NodeKey, Seq[IdentifiableNode]] =
    allNodeKeys.map(_ -> Seq.empty[IdentifiableNode]).toMap

  sealed trait NodeKey {
    def name: String

  }
  object NodeKey {
    case object startEvents extends NodeKey {
      val name = "startEvent"
      override def toString: String = "startEvents"
    }
    case object userTasks extends NodeKey {
      val name = "userTask"
      override def toString: String = "userTasks"
    }
    case object serviceTasks extends NodeKey {
      val name = "serviceTask"
      override def toString: String = "serviceTasks"
    }
    case object businessRuleTasks extends NodeKey {
      val name = "businessRuleTask"
      override def toString: String = "businessRuleTasks"
    }
    case object sendTasks extends NodeKey {
      val name = "sendTask"
      override def toString: String = "sendTasks"
    }
    case object callActivities extends NodeKey {
      val name = "callActivity"
      override def toString: String = "callActivities"
    }
    case object exclusiveGateways extends NodeKey {
      val name = "exclusiveGateway"

      override def toString: String = "exclusiveGateways"
    }
    case object parallelGateways extends NodeKey {
      val name = "parallelGateway"

      override def toString: String = "parallelGateways"
    }
    case object endEvents extends NodeKey {
      val name = "endEvent"
      override def toString: String = "endEvents"
    }
    case object sequenceFlows extends NodeKey {
      val name = "sequenceFlow"
      override def toString: String = "sequenceFlows"
    }
  }
}

trait IdentifiableNode {
  def id: Identifier
}
