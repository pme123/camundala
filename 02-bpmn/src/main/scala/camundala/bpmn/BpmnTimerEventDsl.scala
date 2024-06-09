package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnTimerEventDsl extends BpmnDsl:

  def title: String

  def timerEvent(
  ): TimerEvent =
    TimerEvent(
      title,
      InOutDescr(title, descr = Some(descr))
    )
end BpmnTimerEventDsl
