package camundala.worker.c7zio

import camundala.worker.c7zio.CamundaHelper.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.BadVariableError
import org.camunda.bpm.engine.variable.`type`.ValueType
import org.camunda.bpm.engine.variable.value.TypedValue
import zio.{IO, ZIO}

/** Validator to validate the input variables automatically.
 */
object ProcessVariablesExtractor:

  type VariableType = HelperContext[Seq[IO[BadVariableError, (String, Option[Json])]]]
  type GeneralVariableType = HelperContext[IO[BadVariableError, GeneralVariables]]

  // gets the input variables of the process as Optional Jsons.
  def extract(variableNames: Seq[String]): VariableType =
    variableNames
      .map(k => k -> variableTypedOpt(k))
      .map {
        case k -> Some(typedValue) if typedValue.getType == ValueType.NULL =>
          ZIO.succeed(k -> None) // k -> null as Camunda Expressions need them
        case k -> Some(typedValue) =>
          extractValue(typedValue)
            .map(v => k -> Some(v))
            .mapError(ex => BadVariableError(s"Problem extracting Process Variable $k: ${ex.errorMsg}"))
        case k -> None =>
          ZIO.succeed(k -> None) // k -> null as Camunda Expressions need them
      }
  end extract

  def extractGeneral(generalVariablesFromError: Option[GeneralVariables] = None): GeneralVariableType =
    generalVariablesFromError
      .map(ZIO.succeed(_))
      .getOrElse:
        for
          // mocking
          servicesMocked <- variable(InputParams.servicesMocked, false)
          mockedWorkers <- extractSeqFromArrayOrString(InputParams.mockedWorkers, Seq.empty)
          outputMockOpt <- jsonVariableOpt(InputParams.outputMock)
          outputServiceMockOpt <- jsonVariableOpt(InputParams.outputServiceMock)
          // mapping
          manualOutMapping <- variable(InputParams.manualOutMapping, false)
          outputVariables <- extractSeqFromArrayOrString(InputParams.outputVariables, Seq.empty)
          handledErrors <- extractSeqFromArrayOrString(InputParams.handledErrors, Seq.empty)
          regexHandledErrors <- extractSeqFromArrayOrString(InputParams.regexHandledErrors, Seq.empty)
          // authorization
          impersonateUserIdOpt <- variableOpt[String](InputParams.impersonateUserId)
        yield GeneralVariables(
          servicesMocked = servicesMocked,
          mockedWorkers = mockedWorkers,
          outputMock = outputMockOpt,
          outputServiceMock = outputServiceMockOpt,
          outputVariables = outputVariables,
          manualOutMapping = manualOutMapping,
          handledErrors = handledErrors,
          regexHandledErrors = regexHandledErrors,
          impersonateUserId = impersonateUserIdOpt
        )
  end extractGeneral

end ProcessVariablesExtractor
