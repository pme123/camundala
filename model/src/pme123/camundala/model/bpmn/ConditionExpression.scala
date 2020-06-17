package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.ScriptLanguage.Groovy

sealed trait ConditionExpression {

  def staticFiles: Set[StaticFile] = Set.empty
}

object ConditionExpression {


  case class Expression(value: String) extends ConditionExpression

  case class InlineScript(value: String, language: ScriptLanguage = Groovy) extends ConditionExpression

  case class ExternalScript(ref: StaticFile, language: ScriptLanguage = Groovy) extends ConditionExpression {
    override def staticFiles: Set[StaticFile] = Set(ref)

  }

  case class JsonExpression(jsonStr: String) extends ConditionExpression {

    def value: String =
      s"""
         |$asJson
         |println("JSON STR: "+ "\"\"$jsonStr"\"\")
         |def str = "\"\"$jsonStr"\"\"
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
