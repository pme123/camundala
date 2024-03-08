package camundala.examples.demos.bpmn

import camundala.bpmn.*

object TimerExample extends BpmnProcessDsl:

  lazy val processName: String = "timer-example"
  lazy val descr: String = ""

  lazy val timerProcess = process()

  lazy val timer = timerEvent(
    "wait for one day"
  )
end TimerExample
