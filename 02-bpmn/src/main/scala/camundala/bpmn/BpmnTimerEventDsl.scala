package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnTimerEventDsl extends BpmnDsl:

  def title: String

  def timerEvent(
                  title: String,
                ): TimerEvent =
    TimerEvent(
      title,
      InOutDescr(title, descr = defaultDescr)
    )
end BpmnTimerEventDsl
