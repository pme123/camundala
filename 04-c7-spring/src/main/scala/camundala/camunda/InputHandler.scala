package camundala.camunda

import camundala.domain.*
import org.camunda.bpm.engine.delegate.{DelegateExecution, ExecutionListener}
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity

abstract class InputHandler[T <: Product: InOutCodec]
    extends ExecutionListener, Validator[T], Mocker:

  @throws[Exception]
  override def notify(execution: DelegateExecution): Unit =
    new ExecutionEntity().getProcessInstanceId
    validate(execution)
    mockOrProceed(execution)