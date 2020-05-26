package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropInOutExtensions
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

sealed trait ImplementationTask
  extends ProcessTask {
  def implementation: TaskImplementation
}

case class ServiceTask(id: BpmnNodeId,
                       implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                       extensions: PropInOutExtensions = PropInOutExtensions.none,
                       inOuts: InputOutputs = InputOutputs.none
                      )
  extends ProcessTask
    with ImplementationTask

case class SendTask(id: BpmnNodeId,
                    implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                    extensions: PropInOutExtensions = PropInOutExtensions.none,
                    inOuts: InputOutputs = InputOutputs.none
                   )
  extends ProcessTask
    with ImplementationTask

trait HasForm
  extends ProcessTask {

  def maybeForm: Option[UserTaskForm]

  def staticFiles: Set[StaticFile] = maybeForm.toSet[UserTaskForm].flatMap(_.staticFiles)

}

trait UsersAndGroups {

  def asList(usersAndGroups: String): Seq[String] =
    usersAndGroups.split(",").toList.map(_.trim).filter(_.nonEmpty)
}

case class CandidateGroups(groups:Group*) extends UsersAndGroups {

  def asString(str: String): String =
    (groups.map(_.id.value) ++ asList(str)).distinct.mkString(",")
}

object CandidateGroups {
  val none: CandidateGroups = CandidateGroups()
}

case class CandidateUsers(users: User*) extends UsersAndGroups {
  def asString(str: String): String =
    (users.map(_.username.value) ++ asList(str)).distinct.mkString(",")
}

object CandidateUsers {
  val none: CandidateUsers = CandidateUsers()
}

case class UserTask(id: BpmnNodeId,
                    candidateGroups: CandidateGroups = CandidateGroups.none,
                    candidateUsers: CandidateUsers = CandidateUsers.none,
                    maybeForm: Option[UserTaskForm] = None,
                    extensions: PropInOutExtensions = PropInOutExtensions.none,
                    inOuts: InputOutputs = InputOutputs.none
                   )
  extends ProcessTask
    with HasForm {


}


