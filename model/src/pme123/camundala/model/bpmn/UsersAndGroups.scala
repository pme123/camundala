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
