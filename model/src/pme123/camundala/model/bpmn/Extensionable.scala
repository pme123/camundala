package pme123.camundala.model.bpmn

import eu.timepit.refined.refineV
import pme123.camundala.model.bpmn.ConditionExpression.{Expression, ExternalScript, InlineScript, JsonExpression}
import pme123.camundala.model.bpmn.InputOutput.{InputOutputExpression, InputOutputMap}
import pme123.camundala.model.bpmn.ScriptLanguage.Groovy
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm

trait HasExtProperties {
  def extProperties: ExtProperties
}

trait WithProperties[T] {
  def prop(hasProps: T, key: PropKey, value: String): T
}

object WithProperties {

  def apply[A](implicit withProperties: WithProperties[A]): WithProperties[A] =
    withProperties

  //needed only if we want to support notation: show(...)
  def prop[A: WithProperties](hasProps: A, key: PropKey, value: String): A =
    WithProperties[A].prop(hasProps, key, value)

  //type class instances
  def instance[A](func: (A, PropKey, String) => A): WithProperties[A] =
    (hasProps: A, key: PropKey, value: String) => func(hasProps, key, value)

  implicit val startEvent: WithProperties[StartEvent] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val endEvent: WithProperties[EndEvent] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val userTask: WithProperties[UserTask] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val serviceTask: WithProperties[ServiceTask] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val sendTask: WithProperties[SendTask] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val businessRuleTask: WithProperties[BusinessRuleTask] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val exclusiveGateway: WithProperties[ExclusiveGateway] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val parallelGateway: WithProperties[ParallelGateway] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )
  implicit val sequenceFlow: WithProperties[SequenceFlow] =
    instance((node, key, value) =>
      node.copy(extProperties = node.extProperties :+ Prop(key, value))
    )

}

trait HasExtInOutputs {

  def extInOutputs: ExtInOutputs

  def inOutStaticFiles: Set[StaticFile] = extInOutputs.staticFiles

  protected def inputExpressionExt(
                                    key: PropKey,
                                    expression: String
                                  ): ExtInOutputs = extInOutputs.inputExpression(key, expression)

  def inputInlineExt(key: PropKey, inlineScript: String): ExtInOutputs =
    extInOutputs.inputInline(key, inlineScript)

  def inputJsonExt(key: PropKey, json: String): ExtInOutputs =
    extInOutputs.inputJson(key, json)

  def outputExpressionExt(key: PropKey, expression: String): ExtInOutputs =
    extInOutputs.outputExpression(key, expression)

  def outputInlineExt(key: PropKey, inlineScript: String): ExtInOutputs =
    extInOutputs.outputInline(key, inlineScript)

  def outputJsonExt(key: PropKey, json: String): ExtInOutputs =
    extInOutputs.outputJson(key, json)

}

trait WithInOutputs[T] {
  def extInOutputs(hasInOutputs: T): ExtInOutputs

  def inOutputs(hasInOutputs: T, addInOut: ExtInOutputs): T
}

object WithInOutputs {

  def apply[A](implicit withProperties: WithInOutputs[A]): WithInOutputs[A] =
    withProperties

  //needed only if we want to support notation: show(...)
  def inOutputs[A: WithInOutputs](hasInOuts: A, addInOut: ExtInOutputs): A =
    WithInOutputs[A].inOutputs(hasInOuts, addInOut)

  //type class instances
  def instance[A](
                   func2: A => ExtInOutputs,
                   func: (A, ExtInOutputs) => A
                 ): WithInOutputs[A] =
    new WithInOutputs[A] {
      def inOutputs(hasProps: A, inOut: ExtInOutputs): A =
        func(hasProps, inOut)

      def extInOutputs(hasProps: A): ExtInOutputs = func2(hasProps)
    }

  implicit val userTask: WithInOutputs[UserTask] =
    instance(
      node => node.extInOutputs,
      (node, addInOut: ExtInOutputs) => node.copy(extInOutputs = addInOut)
    )
  implicit val serviceTask: WithInOutputs[ServiceTask] =
    instance(
      node => node.extInOutputs,
      (node, addInOut: ExtInOutputs) => node.copy(extInOutputs = addInOut)
    )
  implicit val sendTask: WithInOutputs[SendTask] =
    instance(
      node => node.extInOutputs,
      (node, addInOut: ExtInOutputs) => node.copy(extInOutputs = addInOut)
    )
  implicit val businessRuleTask: WithInOutputs[BusinessRuleTask] =
    instance(
      node => node.extInOutputs,
      (node, addInOut: ExtInOutputs) => node.copy(extInOutputs = addInOut)
    )
}

case class ExtProperties(properties: Seq[Prop] = Seq.empty) {
  def :+(prop: Prop): ExtProperties = copy(properties = properties :+ prop)
}

object ExtProperties {
  val none: ExtProperties = ExtProperties()
}

case class Prop(key: PropKey, value: String)

case class ExtInOutputs(
                         inputs: Seq[InputOutput] = Nil,
                         outputs: Seq[InputOutput] = Nil
                       ) {

  def inputExpression(key: PropKey, expression: String): ExtInOutputs =
    copy(inputs = inputs :+ InputOutputExpression(key, Expression(expression)))

  def inputStringFromJsonPath(key: PropKey, path: JsonPath): ExtInOutputs =
    copy(inputs = inputs :+ InputOutputExpression.inputStringFromJsonPath(key, path))

  def inputInline(key: PropKey, inlineScript: String): ExtInOutputs =
    copy(inputs =
      inputs :+ InputOutputExpression(key, InlineScript(inlineScript))
    )

  def inputExternal(
                     key: PropKey,
                     scriptPath: FilePath,
                     language: ScriptLanguage = Groovy,
                     includes: Seq[String] = Seq.empty
                   ): ExtInOutputs =
    copy(inputs =
      inputs :+ InputOutputExpression(
        key,
        ExternalScript(StaticFile(scriptPath, includes = includes), language)
      )
    )

  def inputJson(key: PropKey, json: String): ExtInOutputs =
    copy(inputs = inputs :+ InputOutputExpression(key, JsonExpression(json)))

  def inputFromJson(key: PropKey, generatedForm: GeneratedForm): ExtInOutputs =
    copy(inputs =
      inputs :++
        InputOutputExpression.inputFromJson(key, generatedForm)
    )

  def inputFromMap(key: PropKey, generatedForm: GeneratedForm): ExtInOutputs =
    copy(inputs =
      inputs :++
        InputOutputExpression.inputFromMap(key, generatedForm)
    )

  def outputExpression(key: PropKey, expression: String): ExtInOutputs =
    copy(outputs =
      outputs :+ InputOutputExpression(key, Expression(expression))
    )

  def outputMap(key: PropKey, form: GeneratedForm): ExtInOutputs =
    copy(outputs =
      outputs :+
        InputOutputMap.outputToMap(key, form)
    )

  def outputToJson(key: PropKey, form: GeneratedForm): ExtInOutputs =
    copy(outputs =
      outputs :+
        InputOutputExpression.outputToJson(key, form)
    )

  def outputExternal(
                      key: PropKey,
                      scriptPath: FilePath,
                      language: ScriptLanguage = Groovy,
                      includes: Seq[String] = Seq.empty
                    ): ExtInOutputs =
    copy(outputs =
      outputs :+ InputOutputExpression(
        key,
        ExternalScript(StaticFile(scriptPath, includes = includes), language)
      )
    )

  def outputInline(key: PropKey, inlineScript: String): ExtInOutputs =
    copy(outputs =
      outputs :+ InputOutputExpression(key, InlineScript(inlineScript))
    )

  def outputJson(key: PropKey, json: String): ExtInOutputs =
    copy(outputs = outputs :+ InputOutputExpression(key, JsonExpression(json)))

  def staticFiles: Set[StaticFile] =
    inputs.toSet[InputOutput].flatMap {
      case i: InputOutputExpression => i.staticFiles
      case _ => Set.empty
    } ++ outputs.toSet[InputOutput].flatMap {
      case i: InputOutputExpression => i.staticFiles
      case _ => Set.empty
    }

}

object ExtInOutputs {
  def none: ExtInOutputs = ExtInOutputs()
}

sealed trait InputOutput {
  def key: PropKey
}

object InputOutput {

  case class InputOutputExpression(
                                    key: PropKey,
                                    expression: ConditionExpression
                                  ) extends InputOutput {

    def staticFiles: Set[StaticFile] = expression.staticFiles

  }

  object InputOutputExpression {

    def inputStringFromJsonPath(key: PropKey, path: JsonPath): InputOutputExpression =
      InputOutputExpression(key,
        path.toList match {
          case Nil => Expression("No Json Path defined!")// this should not happen > Refined JsonPath requires 2 elements
          case obj :: tail =>
            Expression(s"""$${S($obj)${tail.map(v => s""".prop("$v")""").mkString}.stringValue()}""")
        })

    def inputFromJson(key: PropKey, generatedForm: GeneratedForm): Seq[InputOutputExpression] =
      generatedForm.allFields()
        .filter(_.id.startsWith(s"$key$KeyDelimeter"))
        .map { f =>
          refineV[IdRegex](s"${f.id}")
            .map(InputOutputExpression(_, Expression(s"""$${S($key).prop("${propName(key, f.id)}")}""")))
        }.collect {
        case Right(expr) =>
          expr
      }

    def outputToJson(key: PropKey, generatedForm: GeneratedForm): InputOutputExpression =
      InputOutputExpression(key,
        JsonExpression(
          generatedForm.allFields()
            .filter(_.id.startsWith(s"$key$KeyDelimeter"))
            .map { f =>
              s""""${propName(key, f.id)}": "$$${f.id}""""
            }.mkString("{", ",\n", "}")
        )
      )


    def inputFromMap(key: PropKey, generatedForm: GeneratedForm): Seq[InputOutputExpression] =
      generatedForm.allFields()
        .filter(_.id.startsWith(s"$key$KeyDelimeter"))
        .map { f =>
          refineV[IdRegex](s"${f.id}")
            .map(InputOutputExpression(_, Expression(s"""$${$key.get("${propName(key, f.id)}")}""")))
        }.collect {
        case Right(expr) =>
          expr
      }
  }

  case class InputOutputMap(key: PropKey, entryMap: Map[String, String])
    extends InputOutput

  object InputOutputMap {
    def outputToMap(key: PropKey, form: GeneratedForm): InputOutputMap =
      InputOutputMap(key,
        form.allFields()
          .filter(_.id.startsWith(s"$key$KeyDelimeter"))
          .map { f =>
            propName(key, f.id) -> s"$${${f.id}}"
          }.toMap)
  }

  private def propName(key: PropKey, id: String) = {
    id.replace(s"$key$KeyDelimeter", "")
  }
}
