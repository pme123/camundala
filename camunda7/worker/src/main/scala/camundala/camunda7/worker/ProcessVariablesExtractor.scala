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
object ProcessVariablesExtractor extends CamundaHelper:

  type ValidatorType = HelperContext[Seq[Either[ValidatorError, (String, Option[Json])]]]

  // gets the input variables of the process as Optional Jsons.
  def extract(variableNames: Seq[String]): ValidatorType =
    variableNames
      .map(k => k -> variableTypedOpt(k))
      .map {
        case k -> Some(typedValue) if typedValue.getType == ValueType.NULL =>
          Right(k -> None) // k -> null as Camunda Expressions need them
        case k -> Some(typedValue) =>
          extractValue(typedValue)
            .map(v => k -> Some(v))
            .left
            .map(ex => ValidatorError(s"Problem extracting Process Variable $k: ${ex.errorMsg}"))
        case k -> None =>
          Right(k -> None) // k -> null as Camunda Expressions need them
      }
  end extract
  
end ProcessVariablesExtractor



