package camundala.camunda

import camundala.camunda.TestInitCamundaBpmnApp.run
import os.pwd

object TestInitCamundaBpmnApp extends InitCamundaBpmn, App:

  val projectPath = pwd / "camunda" / "src" / "it"

  run("Test")

end TestInitCamundaBpmnApp
