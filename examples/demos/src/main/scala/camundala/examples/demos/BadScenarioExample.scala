package camundala.examples.demos

import camundala.bpmn.*


object BadScenarioExample extends BpmnDsl:

  lazy val `Bad Scenario with Message`: Process[_, _] =
    process(
      "badScenario-example"
    )
  lazy val `Bad Scenario without Message` =
    `Bad Scenario with Message`

end BadScenarioExample
