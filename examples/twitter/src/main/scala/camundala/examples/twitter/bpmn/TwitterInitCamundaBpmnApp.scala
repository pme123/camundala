package camundala
package examples.twitter.bpmn

import camunda.*
import os.pwd

object TwitterInitCamundaBpmnApp extends InitCamundaBpmn:

  val projectPath = pwd / "examples" / "twitter"

  run("Twitter")

end TwitterInitCamundaBpmnApp


