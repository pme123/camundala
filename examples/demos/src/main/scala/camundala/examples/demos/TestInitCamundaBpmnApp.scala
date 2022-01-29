package camundala.examples.demos

import camundala.camunda.*
import os.pwd

import scala.util.matching.Regex

object TestInitCamundaBpmnApp extends InitCamundaBpmn, App:

  override def avoidCreateIdRegex: Regex = ".+-.+".r

  val projectPath = pwd / "examples" / "demos"

  run("Test")

end TestInitCamundaBpmnApp
