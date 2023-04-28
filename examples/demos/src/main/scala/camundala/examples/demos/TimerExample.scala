package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

import java.time.LocalDateTime

object TimerExample extends BpmnDsl:

  lazy val timerProcess = process(
    "timer-example"
  )

  lazy val timer = timerEvent(
    "wait for one day"
  )
end TimerExample
