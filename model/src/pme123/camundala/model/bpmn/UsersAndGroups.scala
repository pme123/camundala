package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

case class User(username: Username, maybeName: Option[String] = None, maybeFirstName: Option[String] = None, maybeEmail: Option[Email] = None, groups: Seq[Group] = Nil) {
  def name(name: String): User = copy(maybeName = Some(name))

  def firstName(name: String): User = copy(maybeFirstName = Some(name))

  def email(email: Email): User = copy(maybeEmail = Some(email))

  def group(group: Group): User = copy(groups = groups :+ group)

}

case class Group(id: Identifier, maybeName: Option[String] = None, `type`: Identifier = Group.Camundala) {
  def name(name: String): Group = copy(maybeName = Some(name))

  def groupType(groupType: Identifier): Group = copy(`type` = groupType)
}

object Group {
  val Camundala: Identifier = "Camundala"
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
