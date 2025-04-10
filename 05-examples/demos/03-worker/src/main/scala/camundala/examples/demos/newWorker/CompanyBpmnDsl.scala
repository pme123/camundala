package camundala.examples.demos.newWorker

import camundala.domain.{
  BpmnCustomTaskDsl,
  BpmnDecisionDsl,
  BpmnDsl,
  BpmnMessageEventDsl,
  BpmnProcessDsl,
  BpmnServiceTaskDsl,
  BpmnSignalEventDsl,
  BpmnTimerEventDsl,
  BpmnUserTaskDsl
}

trait CompanyBpmnDsl extends BpmnDsl:

end CompanyBpmnDsl

trait CompanyBpmnProcessDsl      extends BpmnProcessDsl, CompanyBpmnDsl
trait CompanyBpmnServiceTaskDsl  extends BpmnServiceTaskDsl, CompanyBpmnDsl
trait CompanyBpmnCustomTaskDsl   extends BpmnCustomTaskDsl, CompanyBpmnDsl
trait CompanyBpmnDecisionDsl     extends BpmnDecisionDsl, CompanyBpmnDsl
trait CompanyBpmnUserTaskDsl     extends BpmnUserTaskDsl, CompanyBpmnDsl
trait CompanyBpmnMessageEventDsl extends BpmnMessageEventDsl, CompanyBpmnDsl
trait CompanyBpmnSignalEventDsl  extends BpmnSignalEventDsl, CompanyBpmnDsl
trait CompanyBpmnTimerEventDsl   extends BpmnTimerEventDsl, CompanyBpmnDsl
