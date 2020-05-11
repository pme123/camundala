package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ScriptLanguage.ScalaScript

sealed trait ConditionExpression {
  def value: String
}

object ConditionExpression {

  case class Expression(value: String) extends ConditionExpression

  case class InlineScript(value: String, language: ScriptLanguage = ScalaScript) extends ConditionExpression

  // case class ExternalScript() extends ConditionExpression
}
sealed trait ScriptLanguage {
  def key: String
}
object ScriptLanguage {
  case object ScalaScript extends ScriptLanguage {
    final val key: String = "scala"
  }
}