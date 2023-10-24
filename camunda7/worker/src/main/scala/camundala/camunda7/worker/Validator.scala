package camundala
package camunda7.worker

import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ValidatorError
import camundala.domain.*
import io.circe.JsonObject
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

/** Validator to validate the input variables automatically.
  */
trait Validator[In <: Product: CirceCodec] extends CamundaHelper:

  protected def prototype: In

  type ValidatorType = HelperContext[Either[ValidatorError, In]]

  // by default it does no additional validation
  protected def validate(in: In): ValidatorType = Right(in)

  // gets the inputs of the process and creates the In object.
  // if it can not create the object, according messages are created and a ValidaterError created.
  protected def validate(): ValidatorType =
    val jsonResult: Either[ValidatorError, Seq[(String, Option[Json])]] =
      prototype.productElementNames.toSeq
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
        .partition(_.isRight) match
        case (successes, failures) if failures.isEmpty =>
          Right(
            successes.collect { case Right(value) => value }
          )
        case (_, failures) =>
          Left(
            ValidatorError(
              failures
                .collect { case Left(value) => value }
                .mkString("Validater Error(s):\n - ", " - ", "\n"),

            )
          )
    println("Input Variables for validation:")
    val json: Either[ValidatorError, JsonObject] = jsonResult
      .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
        println(s" - $jsonKey: ${jsonValue.getClass.getSimpleName} - $jsonValue")
        jsonObj.add(jsonKey, jsonValue.getOrElse(Json.Null))
      })
    json
      .flatMap: jsonObj => 
        decodeTo[In](jsonObj.asJson.toString)
        .left
          .map(ex =>
            ValidatorError(errorMsg = ex.errorMsg))
          .flatMap(validate)
  end validate


