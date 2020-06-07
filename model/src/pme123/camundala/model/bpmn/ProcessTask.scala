package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, ExternalTask}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm

sealed trait ProcessTask
  extends BpmnNode
    with HasExtProperties
    with HasExtInOutputs

sealed trait ImplementationTask
  extends ProcessTask {

  def implementation: TaskImplementation
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

  // HasExtProperties
  def prop(prop: (PropKey, String)): ServiceTask =
    copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

  // HasExtInOutputs
  def inputExpression(key: PropKey, expression: String): ServiceTask = copy(extInOutputs = extInOutputs.inputExpression(key, expression))

  def inputInline(key: PropKey, inlineScript: String): ServiceTask = copy(extInOutputs = extInOutputs.inputExpression(key, inlineScript))

  def inputJson(key: PropKey, json: String): ServiceTask = copy(extInOutputs = extInOutputs.inputExpression(key, json))

  def outputExpression(key: PropKey, expression: String): ServiceTask = copy(extInOutputs = extInOutputs.outputExpression(key, expression))

  def outputInline(key: PropKey, inlineScript: String): ServiceTask = copy(extInOutputs = extInOutputs.outputExpression(key, inlineScript))

  def outputJson(key: PropKey, json: String): ServiceTask = copy(extInOutputs = extInOutputs.outputExpression(key, json))

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

  // HasExtProperties
  def prop(prop: (PropKey, String)): SendTask =
    copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

  // HasExtInOutputs
  def inputExpression(key: PropKey, expression: String): SendTask = copy(extInOutputs = extInOutputs.inputExpression(key, expression))

  def inputInline(key: PropKey, inlineScript: String): SendTask = copy(extInOutputs = extInOutputs.inputExpression(key, inlineScript))

  def inputJson(key: PropKey, json: String): SendTask = copy(extInOutputs = extInOutputs.inputExpression(key, json))

  def outputExpression(key: PropKey, expression: String): SendTask = copy(extInOutputs = extInOutputs.outputExpression(key, expression))

  def outputInline(key: PropKey, inlineScript: String): SendTask = copy(extInOutputs = extInOutputs.outputExpression(key, inlineScript))

  def outputJson(key: PropKey, json: String): SendTask = copy(extInOutputs = extInOutputs.outputExpression(key, json))

}

trait HasForm
  extends BpmnNode {

  def maybeForm: Option[UserTaskForm]

  def staticFiles: Set[StaticFile] = maybeForm.toSet[UserTaskForm].flatMap(_.staticFiles)

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

  def form(form: UserTaskForm): UserTask = copy(maybeForm = Some(form))

  def embeddedForm(fileName: FilePath, resourcePath: PathElem): UserTask =
    copy(maybeForm = Some(EmbeddedDeploymentForm(StaticFile(fileName, resourcePath))))

  // HasExtProperties
  def prop(prop: (PropKey, String)): UserTask =
    copy(extProperties = extProperties :+ Prop(prop._1, prop._2))

  // HasExtInOutputs
  def inputExpression(key: PropKey, expression: String): UserTask = copy(extInOutputs = extInOutputs.inputExpression(key, expression))

  def inputInline(key: PropKey, inlineScript: String): UserTask = copy(extInOutputs = extInOutputs.inputExpression(key, inlineScript))

  def inputJson(key: PropKey, json: String): UserTask = copy(extInOutputs = extInOutputs.inputExpression(key, json))

  def outputExpression(key: PropKey, expression: String): UserTask = copy(extInOutputs = extInOutputs.outputExpression(key, expression))

  def outputInline(key: PropKey, inlineScript: String): UserTask = copy(extInOutputs = extInOutputs.outputExpression(key, inlineScript))

  def outputJson(key: PropKey, json: String): UserTask = copy(extInOutputs = extInOutputs.outputExpression(key, json))

}


