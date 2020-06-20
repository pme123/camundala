package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, DmnImpl, ExternalTask}

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
                       extInOutputs: ExtInOutputs = ExtInOutputs.none
                      )
  extends ProcessTask
    with ImplementationTask {

  def delegate(expression: String): ServiceTask = copy(implementation = DelegateExpression(expression))

  def external(topic: String): ServiceTask = copy(implementation = ExternalTask(topic))

}

case class SendTask(id: BpmnNodeId,
                    implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none
                   )
  extends ProcessTask
    with ImplementationTask {

  def delegate(expression: String): SendTask = copy(implementation = DelegateExpression(expression))

  def external(topic: String): SendTask = copy(implementation = ExternalTask(topic))

}

trait UsersAndGroups {

  def asList(usersAndGroups: String): Seq[String] =
    usersAndGroups.split(",").toList.map(_.trim).filter(_.nonEmpty)
}

case class CandidateGroups(groups: Group*) extends UsersAndGroups {

  def asString(str: String): String =
    (groups.map(_.id.value) ++ asList(str)).distinct.mkString(",")

  def :+(group: Group): CandidateGroups = CandidateGroups(groups = groups :+ group: _*)

}

object CandidateGroups {
  val none: CandidateGroups = CandidateGroups()
}

case class CandidateUsers(users: User*) extends UsersAndGroups {
  def asString(str: String): String =
    (users.map(_.username.value) ++ asList(str)).distinct.mkString(",")

  def :+(user: User): CandidateUsers = CandidateUsers(users = users :+ user: _*)

}

object CandidateUsers {
  val none: CandidateUsers = CandidateUsers()
}

case class UserTask(id: BpmnNodeId,
                    candidateGroups: CandidateGroups = CandidateGroups.none,
                    candidateUsers: CandidateUsers = CandidateUsers.none,
                    maybeForm: Option[UserTaskForm] = None,
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none
                   )
  extends ProcessTask
    with HasForm {

  def groups(): Seq[Group] = candidateGroups.groups

  def users(): Seq[User] = candidateUsers.users

  def candidateGroup(group: Group): UserTask = copy(candidateGroups = candidateGroups :+ group)

  def candidateUser(user: User): UserTask = copy(candidateUsers = candidateUsers :+ user)

}

//<businessRuleTask id="CountryRiskTask" name="Country Risk" camunda:asyncBefore="true" camunda:asyncAfter="true" camunda:resultVariable="approvalRequired" camunda:decisionRef="country-risk" camunda:mapDecisionResult="singleEntry">
// only implementation DMN supported
case class BusinessRuleTask(id: BpmnNodeId,
                            implementation: TaskImplementation = DmnImpl("yourDMN"),
                            extProperties: ExtProperties = ExtProperties.none,
                            extInOutputs: ExtInOutputs = ExtInOutputs.none
                           )
  extends ProcessTask
    with ImplementationTask {

  def dmn(decisionRef: FilePath, resultVariable: Identifier = "ruleResult"): BusinessRuleTask = copy(implementation =
    DmnImpl(StaticFile(decisionRef), resultVariable))

}

