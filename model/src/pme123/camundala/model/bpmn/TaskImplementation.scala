package pme123.camundala.model.bpmn

sealed trait TaskImplementation

object TaskImplementation {
  case class DelegateExpression(expresssion: String)
    extends TaskImplementation

  case class ExternalTask(topic: String)
    extends TaskImplementation
}
