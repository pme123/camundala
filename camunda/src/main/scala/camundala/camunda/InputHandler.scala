package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution, ExecutionListener}
import org.camunda.bpm.engine.variable.`type`.{FileValueType, PrimitiveValueType, SerializableValueType}
import org.camunda.bpm.engine.variable.impl.`type`.PrimitiveValueTypeImpl.StringTypeImpl
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl
import org.camunda.bpm.engine.variable.value.TypedValue
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

abstract class InputHandler[T <: Product : Encoder : Decoder]
  extends ExecutionListener, Validator[T], Mocker :

  @throws[Exception]
  override def notify(execution: DelegateExecution): Unit =
    validate(execution)
    mockOrProceed(execution)