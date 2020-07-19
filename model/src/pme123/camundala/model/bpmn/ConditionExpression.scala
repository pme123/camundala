package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ScriptLanguage.Groovy

sealed trait ConditionExpression {

  def value: String

  def staticFiles: Set[StaticFile] = Set.empty
}

object ConditionExpression {


  case class Expression(value: String) extends ConditionExpression

  case class InlineScript(value: String, language: ScriptLanguage = Groovy) extends ConditionExpression

  case class ExternalScript(ref: StaticFile, language: ScriptLanguage = Groovy) extends ConditionExpression {
    override def staticFiles: Set[StaticFile] = Set(ref)

    val value = "" // there is no value
  }

  case class JsonExpression(jsonStr: String, variables: VariableDefs = VariableDefs.none) extends ConditionExpression {

    def value: String =
      s"""
         |$asJson
         |
         |$variables
         |
         |def str = "\"\"$jsonStr"\"\"
         |println("JSON STR: "+ str)
         |asJson(str)
         |""".stripMargin

  }

  val asJson: String =
    """
      |import static org.camunda.spin.Spin.*
      |import groovy.json.*
      |
      |def asJson(String jsonStr) {
      |    jsonSlurper = new JsonSlurper()
      |    json = jsonSlurper.parseText(jsonStr)
      |
      |    S(JsonOutput.toJson(json))
      |}""".stripMargin

  case class DynJsonExpression(groovyJsonStr: String, variables: VariableDefs = VariableDefs.none) extends ConditionExpression {

    def value: String =
      s"""
         |import groovy.json.JsonOutput
         |import static org.camunda.spin.Spin.*
         |
         |$variables
         |
         |result = JsonOutput.toJson($groovyJsonStr)
         |println("Groovy String: $$result")
         |S(result)
         |""".stripMargin

  }

  object DynJsonExpression {
    def apply(vars: Map[String, String]): DynJsonExpression =
      DynJsonExpression(s"""[${vars.map { case (k, v) => s""""$k": $v""" }.mkString(",\n")}]""")
  }

  // case class ExternalScript() extends ConditionExpression
}

sealed trait ScriptLanguage {
  def key: String
}

object ScriptLanguage {

  case object ScalaScript extends ScriptLanguage {
    final val key: String = "scala"
  }

  case object Groovy extends ScriptLanguage {
    final val key: String = "groovy"
  }

}
