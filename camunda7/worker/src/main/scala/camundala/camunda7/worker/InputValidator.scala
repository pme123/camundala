package camundala.camunda7.worker

import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ValidatorError
import camundala.domain.*
import camundala.worker.InValidator
import io.circe.JsonObject
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

/** Validator to validate the input variables automatically.
  */
case class InputValidator(inputValidator: InValidator[?]) extends CamundaHelper:

  type ValidatorType = HelperContext[Either[ValidatorError, ?]]

  // gets the inputs of the process and creates the In object.
  // if it can not create the object, according messages are created and a ValidaterError created.
  def validate(): ValidatorType =
    val jsons: Seq[Either[Any, (String, Option[Json])]] = inputValidator.inElementNames
      .map(k => k -> variableTypedOpt(k))
      .map {
        case k -> Some(typedValue) if typedValue.getType == ValueType.NULL =>
          Right(k -> None) // k -> null as Camunda Expressions need them
        case k -> Some(typedValue) =>
          extractValue(typedValue)
            .map(v => k -> Some(v))
        case k -> None =>
          Right(k -> None) // k -> null as Camunda Expressions need them

      }
    inputValidator.validate(jsons)
  end validate


