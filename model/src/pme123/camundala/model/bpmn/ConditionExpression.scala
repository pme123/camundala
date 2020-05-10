package pme123.camundala.model.bpmn

sealed trait ConditionExpression {

}
object ConditionExpression {
  case class Expression(value: String) extends ConditionExpression

 // case class InlineScript(language: ScriptLanguage) extends ConditionExpression
 // case class ExternalScript() extends ConditionExpression
}
