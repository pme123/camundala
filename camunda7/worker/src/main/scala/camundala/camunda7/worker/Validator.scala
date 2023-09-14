package camundala
package camunda7.worker

import domain.*
import camundala.camunda7.worker.CamundalaWorkerError.ValidaterError
import io.circe.JsonObject
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

/** Validator to validate the input variables automatically.
  */
trait Validator[In <: Product: CirceCodec] extends CamundaHelper:

  protected def prototype: In

  type ValidatorType = HelperContext[Either[ValidaterError, In]]

  /** If valid -> Right(true) if skipped -> Right(false) if not valid ->
    * Left(String with validation errors)
    *
    * @param getVariableTyped
    * @return
    */
  protected def validate(): ValidatorType =
    val jsonResult: Either[ValidaterError, Seq[(String, Option[Json])]] =
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
            ValidaterError(
              failures
                .collect { case Left(value) => value }
                .mkString("Validater Error(s):\n - ", " - ", "\n"),

            )
          )
    println("Input Variables for validation:")
    val json: Either[ValidaterError, JsonObject] = jsonResult
      .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
        println(s" - $jsonKey: ${jsonValue.getClass.getSimpleName} - $jsonValue")
        jsonObj.add(jsonKey, jsonValue.getOrElse(Json.Null))
      })
    json
      .flatMap: jsonObj => 
        decodeTo[In](jsonObj.asJson.toString)
        .left
          .map(ex =>
            ValidaterError(errorMsg = ex.errorMsg))
        
  end validate


