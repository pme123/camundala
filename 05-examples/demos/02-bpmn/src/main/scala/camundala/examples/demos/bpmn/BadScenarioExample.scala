package camundala.examples.demos.bpmn

import camundala.bpmn.*

object BadScenarioExample extends BpmnProcessDsl:
  val processName = "badScenario-example"
  val descr       = ""

  lazy val `Bad Scenario with Message`    =
    process()
  lazy val `Bad Scenario without Message` =
    `Bad Scenario with Message`

end BadScenarioExample
