package camundala.dsl

case class Parameters(params: Seq[Parameter] = Seq.empty) {

  def :+(p: Parameter): Parameters = copy(params :+ p)
}

object Parameters {
  val none: Parameters = Parameters()
}

sealed trait Parameter

case class TextParam(key:Identifier, expr: String) extends Parameter

case class ScriptParam(key:Identifier, script: String, format: String) extends Parameter

case class ScriptRefParam(key:Identifier, ref: String, format: String) extends Parameter
