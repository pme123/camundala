package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

case class User(username: Username, name: String, firstName: String, email: Email, groups: Seq[Group] = Nil)

case class Group(id: Identifier, name: String, `type`: Identifier = Group.Camundala)

object Group {
  val Camundala: Identifier = "Camundala"
}