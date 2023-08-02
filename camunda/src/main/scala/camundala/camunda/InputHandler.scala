package camundala.camunda

import camundala.domain.*
import org.camunda.bpm.engine.delegate.{DelegateExecution, ExecutionListener}


abstract class InputHandler[T <: Product : CirceCodec]
  extends ExecutionListener, Validator[T], Mocker :

  @throws[Exception]
  override def notify(execution: DelegateExecution): Unit =
    validate(execution)
    mockOrProceed(execution)