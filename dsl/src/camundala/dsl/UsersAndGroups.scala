package camundala.dsl

import eu.timepit.refined.auto._

case class BpmnUser(username: Identifier, maybeName: Option[String] = None, maybeFirstName: Option[String] = None, maybeEmail: Option[Email] = None, groups: Seq[BpmnGroup] = Nil) {
  def name(name: String): BpmnUser = copy(maybeName = Some(name))

  def firstName(name: String): BpmnUser = copy(maybeFirstName = Some(name))

  def email(email: Email): BpmnUser = copy(maybeEmail = Some(email))

  def isInGroups(group: BpmnGroup, groups: BpmnGroup*): BpmnUser = copy(groups = (groups :+ group) ++ groups)

}

case class BpmnGroup(id: Identifier, maybeName: Option[String] = None, `type`: Identifier = BpmnGroup.Camundala) {
  def name(name: String): BpmnGroup = copy(maybeName = Some(name))

  def groupType(groupType: Identifier): BpmnGroup = copy(`type` = groupType)
}

object BpmnGroup {
  val Camundala: Identifier = "Camundala"
}

case class CandidateGroups(groups: Seq[BpmnGroup] = Nil)

object CandidateGroups {
  val none: CandidateGroups = CandidateGroups()
}

case class CandidateUsers(users: Seq[BpmnUser] = Nil)

object CandidateUsers {
  val none: CandidateUsers = CandidateUsers()
}
