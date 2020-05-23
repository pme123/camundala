package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ScriptLanguage.ScalaScript

sealed trait ConditionExpression {
  def value: String
}

object ConditionExpression {

  case class Expression(value: String) extends ConditionExpression

  case class InlineScript(value: String, language: ScriptLanguage = ScalaScript) extends ConditionExpression

  case class JsonExpression(jsonStr: String) extends ConditionExpression {
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

    override def value: String =
      s"""
         |$asJson
         |println("JSON STR: "+ "\"\"$jsonStr"\"\")
         |def str = "\"\"$jsonStr"\"\"
         |asJson(str)
         |""".stripMargin

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

}