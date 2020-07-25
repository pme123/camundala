package camundala.dsl

sealed trait Condition

object Condition {
  val Groovy = "groovy"
  val Javascript = "javascript"
}

case class ExpressionCond(expr: String) extends Condition

case class ScriptCond(script: String, format: String) extends Condition

case class ScriptRefCond(ref: String, format: String) extends Condition
