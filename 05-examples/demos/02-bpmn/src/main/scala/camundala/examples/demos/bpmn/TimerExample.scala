package camundala.examples.demos.bpmn

import camundala.domain.{BpmnProcessDsl, BpmnTimerEventDsl}

object TimerExample extends BpmnProcessDsl:

  lazy val processName: String = "timer-example"
  lazy val descr: String = ""

  lazy val example = process()

end TimerExample

object TheTimer extends BpmnTimerEventDsl:

  lazy val title: String = "the timer event"
  lazy val descr: String = ""

  lazy val example = timerEvent()

end TheTimer