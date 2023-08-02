package camundala.examples.demos

import camundala.bpmn.*

object TimerExample extends BpmnDsl:

  lazy val timerProcess = process(
    "timer-example"
  )

  lazy val timer = timerEvent(
    "wait for one day"
  )
end TimerExample
