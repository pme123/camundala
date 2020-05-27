package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

case class User(username: Username, maybeName: Option[String] = None, maybeFirstName: Option[String] = None, maybeEmail: Option[Email] = None, groups: Seq[Group] = Nil)

case class Group(id: Identifier, maybeName: Option[String] = None, `type`: Identifier = Group.Camundala)

object Group {
  val Camundala: Identifier = "Camundala"
}