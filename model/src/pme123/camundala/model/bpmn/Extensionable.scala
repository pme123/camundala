package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ConditionExpression.{Expression, ExternalScript, InlineScript, JsonExpression}
import pme123.camundala.model.bpmn.ScriptLanguage.Groovy


trait HasExtProperties {

  def extProperties: ExtProperties
}

trait WithProperties[T] {
  def prop(hasProps: T, key: PropKey, value: String): T
}

object WithProperties {

  def apply[A](implicit withProperties: WithProperties[A]): WithProperties[A] = withProperties

  //needed only if we want to support notation: show(...)
  def prop[A: WithProperties](hasProps: A, key: PropKey, value: String): A =
    WithProperties[A].prop(hasProps, key, value)

  //type class instances
  def instance[A](func: (A, PropKey, String) => A): WithProperties[A] =
    new WithProperties[A] {
      def prop(hasProps: A, key: PropKey, value: String): A =
        func(hasProps, key, value)
    }

  implicit val startEvent: WithProperties[StartEvent] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val endEvent: WithProperties[EndEvent] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val userTask: WithProperties[UserTask] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val serviceTask: WithProperties[ServiceTask] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val sendTask: WithProperties[SendTask] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val businessRuleTask: WithProperties[BusinessRuleTask] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val exclusiveGateway: WithProperties[ExclusiveGateway] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val parallelGateway: WithProperties[ParallelGateway] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))
  implicit val sequenceFlow: WithProperties[SequenceFlow] =
    instance((node, key, value) => node.copy(extProperties = node.extProperties :+ Prop(key, value)))

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

