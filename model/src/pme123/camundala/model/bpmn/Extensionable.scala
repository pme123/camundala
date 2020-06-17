package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ConditionExpression.{Expression, ExternalScript, InlineScript, JsonExpression}
import pme123.camundala.model.bpmn.ScriptLanguage.Groovy


trait HasExtProperties {

  def extProperties: ExtProperties
}

trait HasExtInOutputs {

  def extInOutputs: ExtInOutputs

  def inOutStaticFiles: Set[StaticFile] = extInOutputs.staticFiles

}

case class ExtProperties(properties: Seq[Prop] = Seq.empty) {
  def :+(prop: Prop): ExtProperties = copy(properties = properties :+ prop)
}

object ExtProperties {
  val none: ExtProperties = ExtProperties()
}

case class Prop(key: PropKey, value: String)

case class ExtInOutputs(inputs: Seq[InputOutput] = Nil, outputs: Seq[InputOutput] = Nil) {
  val inputMap: Map[PropKey, ConditionExpression] = inputs.map(in => in.key -> in.expression).toMap
  val outputMap: Map[PropKey, ConditionExpression] = outputs.map(out => out.key -> out.expression).toMap

  def inputExpression(key: PropKey, expression: String): ExtInOutputs = copy(inputs = inputs :+ InputOutput(key, Expression(expression)))

  def inputInline(key: PropKey, inlineScript: String): ExtInOutputs = copy(inputs = inputs :+ InputOutput(key, InlineScript(inlineScript)))

  def inputExternal(key: PropKey, scriptPath: FilePath, language: ScriptLanguage = Groovy, includes: Seq[String] = Seq.empty): ExtInOutputs = copy(inputs = inputs :+ InputOutput(key, ExternalScript(StaticFile(scriptPath, includes = includes), language)))

  def inputJson(key: PropKey, json: String): ExtInOutputs = copy(inputs = inputs :+ InputOutput(key, JsonExpression(json)))

  def outputExpression(key: PropKey, expression: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, Expression(expression)))

  def outputInline(key: PropKey, inlineScript: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, InlineScript(inlineScript)))

  def outputJson(key: PropKey, json: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, JsonExpression(json)))

  def staticFiles: Set[StaticFile] = inputs.toSet[InputOutput].flatMap(_.staticFiles) ++ outputs.toSet[InputOutput].flatMap(_.staticFiles)

}

object ExtInOutputs {
  def none: ExtInOutputs = ExtInOutputs()
}

case class InputOutput(key: PropKey, expression: ConditionExpression) {

  def staticFiles: Set[StaticFile] = expression.staticFiles

}

