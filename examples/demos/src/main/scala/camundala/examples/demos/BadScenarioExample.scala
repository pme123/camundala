package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

import java.time.LocalDateTime

object BadScenarioExample extends BpmnDsl:

  lazy val `Bad Scenario with Message`: Process[_, _] =
    process(
      "badScenario-example"
    )
  lazy val `Bad Scenario without Message` =
    `Bad Scenario with Message`

end BadScenarioExample
