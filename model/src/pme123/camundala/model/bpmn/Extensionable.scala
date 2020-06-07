package pme123.camundala.model.bpmn

import com.softwaremill.quicklens._
import pme123.camundala.model.bpmn.ConditionExpression.{Expression, InlineScript, JsonExpression}


trait HasExtProperties {

  def extProperties: ExtProperties
}

trait HasExtInOutputs {

  def extInOutputs: ExtInOutputs

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

  def inputJson(key: PropKey, json: String): ExtInOutputs = copy(inputs = inputs :+ InputOutput(key, JsonExpression(json)))

  def outputExpression(key: PropKey, expression: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, Expression(expression)))

  def outputInline(key: PropKey, inlineScript: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, InlineScript(inlineScript)))

  def outputJson(key: PropKey, json: String): ExtInOutputs = copy(outputs = outputs :+ InputOutput(key, JsonExpression(json)))


}

object ExtInOutputs {
  def none: ExtInOutputs = ExtInOutputs()
}

case class InputOutput(key: PropKey, expression: ConditionExpression)

