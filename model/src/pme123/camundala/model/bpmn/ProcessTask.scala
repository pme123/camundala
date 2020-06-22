package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, DmnImpl}

sealed trait ProcessTask
  extends BpmnNode
    with HasExtProperties
    with HasExtInOutputs {
}

sealed trait ImplementationTask
  extends ProcessTask {

  def implementation: TaskImplementation

  def implStaticFiles: Set[StaticFile] = implementation.staticFiles

}

case class ServiceTask(id: BpmnNodeId,
                       implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                       extProperties: ExtProperties = ExtProperties.none,
                       extInOutputs: ExtInOutputs = ExtInOutputs.none,
                       inFlows: Seq[SequenceFlow] = Seq.empty,
                       outFlows: Seq[SequenceFlow] = Seq.empty
                      )
  extends ProcessTask
    with ImplementationTask
    with HasInFlows
    with HasOutFlows {
}

case class SendTask(id: BpmnNodeId,
                    implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty,
                    outFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends ProcessTask
    with ImplementationTask
    with HasInFlows
    with HasOutFlows {
}

trait UsersAndGroups {

  def asList(usersAndGroups: String): Seq[String] =
    usersAndGroups.split(",").toList.map(_.trim).filter(_.nonEmpty)
}

case class CandidateGroups(groups: Group*) extends UsersAndGroups {

  def asString(str: String): String =
    (groups.map(_.id.value) ++ asList(str)).distinct.mkString(",")

  def ++(candGroups: Seq[Group]): CandidateGroups = CandidateGroups(groups = groups ++ candGroups: _*)

}

object CandidateGroups {
  val none: CandidateGroups = CandidateGroups()
}

case class CandidateUsers(users: User*) extends UsersAndGroups {
  def asString(str: String): String =
    (users.map(_.username.value) ++ asList(str)).distinct.mkString(",")

  def ++(candUsers: Seq[User]): CandidateUsers = CandidateUsers(users = users ++ candUsers: _*)

}

object CandidateUsers {
  val none: CandidateUsers = CandidateUsers()
}

case class UserTask(id: BpmnNodeId,
                    candidateGroups: CandidateGroups = CandidateGroups.none,
                    candidateUsers: CandidateUsers = CandidateUsers.none,
                    maybeForm: Option[UserTaskForm] = None,
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty,
                    outFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends ProcessTask
    with HasForm
    with HasInFlows
    with HasOutFlows {

  def groups(): Seq[Group] = candidateGroups.groups

  def users(): Seq[User] = candidateUsers.users

  def candidateGroups(candGroups: Group*): UserTask = copy(candidateGroups = candidateGroups ++ candGroups)

  def candidateGroup(group: Group): UserTask = candidateGroups(group)

  def candidateUsers(users: User*): UserTask = copy(candidateUsers = candidateUsers ++ users)

  def candidateUser(user: User): UserTask = candidateUsers(user)

}

//<businessRuleTask id="CountryRiskTask" name="Country Risk" camunda:asyncBefore="true" camunda:asyncAfter="true" camunda:resultVariable="approvalRequired" camunda:decisionRef="country-risk" camunda:mapDecisionResult="singleEntry">
// only implementation DMN supported
case class BusinessRuleTask(id: BpmnNodeId,
                            implementation: TaskImplementation = DmnImpl("yourDMN"),
                            extProperties: ExtProperties = ExtProperties.none,
                            extInOutputs: ExtInOutputs = ExtInOutputs.none,
                            inFlows: Seq[SequenceFlow] = Seq.empty,
                            outFlows: Seq[SequenceFlow] = Seq.empty
                           )
  extends ProcessTask
    with ImplementationTask
    with HasInFlows
    with HasOutFlows {

  override def generateDsl(): String =
    super.generateDsl() +
      generateInFlowDsl +
      generateOutFlowDsl

  def dmn(decisionRef: FilePath, resultVariable: Identifier = "ruleResult"): BusinessRuleTask = copy(implementation =
    DmnImpl(StaticFile(decisionRef), resultVariable))

}

