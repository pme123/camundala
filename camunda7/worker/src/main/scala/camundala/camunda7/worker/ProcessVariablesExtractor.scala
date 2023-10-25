package camundala.camunda7.worker

import camundala.bpmn.ErrorCodes
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.{BadVariableError, ValidatorError}
import camundala.domain.*
import camundala.bpmn.*
import io.circe.JsonObject
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.engine.variable.`type`.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue

/** Validator to validate the input variables automatically.
  */
object ProcessVariablesExtractor extends CamundaHelper:

  type VariableType = HelperContext[Seq[Either[BadVariableError, (String, Option[Json])]]]
  type GeneralVariableType = HelperContext[Either[BadVariableError, GeneralVariables]]

  // gets the input variables of the process as Optional Jsons.
  def extract(variableNames: Seq[String]): VariableType =
    variableNames
      .map(k => k -> variableTypedOpt(k))
      .map {
        case k -> Some(typedValue) if typedValue.getType == ValueType.NULL =>
          Right(k -> None) // k -> null as Camunda Expressions need them
        case k -> Some(typedValue) =>
          extractValue(typedValue)
            .map(v => k -> Some(v))
            .left
            .map(ex => BadVariableError(s"Problem extracting Process Variable $k: ${ex.errorMsg}"))
        case k -> None =>
          Right(k -> None) // k -> null as Camunda Expressions need them
      }
  end extract

  private def defaultHandledErrorCodes: Seq[ErrorCodes] =
    Seq(ErrorCodes.`output-mocked`, ErrorCodes.`validation-failed`)

  def extractGeneral(): GeneralVariableType =
    for {
      servicesMocked <- variable(InputParams.servicesMocked, false)
      outputMockOpt <- jsonVariableOpt(InputParams.outputMock)
      outputServiceMockOpt <- jsonVariableOpt(InputParams.outputServiceMock)
      mockedSubprocesses <- extractSeqFromArrayOrString(InputParams.mockedSubprocesses, Seq.empty)
      outputVariables <- extractSeqFromArrayOrString(InputParams.mockedSubprocesses, Seq.empty)
      handledErrors <- extractSeqFromArrayOrString(InputParams.handledErrors, defaultHandledErrorCodes)
      regexHandledErrors <- extractSeqFromArrayOrString(InputParams.regexHandledErrors, Seq.empty)
      impersonateUserIdOpt <- variableOpt[String](InputParams.impersonateUserId)
      serviceNameOpt <- variableOpt[String](InputParams.serviceName)
    } yield GeneralVariables(
      servicesMocked = servicesMocked,
      outputMockOpt = outputMockOpt,
      outputServiceMockOpt = outputServiceMockOpt,
      mockedSubprocesses = mockedSubprocesses,
      outputVariables = outputVariables,
      handledErrors = handledErrors,
      regexHandledErrors = regexHandledErrors,
      impersonateUserIdOpt = impersonateUserIdOpt,
      serviceNameOpt: Option[String]
    )
  end extractGeneral
end ProcessVariablesExtractor



