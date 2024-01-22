package camundala.examples.demos.bpmn

import camundala.bpmn.*


object BadScenarioExample extends BpmnDsl:

  lazy val `Bad Scenario with Message` =
    process(
      "badScenario-example"
    )
  lazy val `Bad Scenario without Message` =
    `Bad Scenario with Message`

end BadScenarioExample
