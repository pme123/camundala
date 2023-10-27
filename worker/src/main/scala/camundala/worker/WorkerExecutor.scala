package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*

case class WorkerExecutor[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    T <: Worker[In, Out, ?]
](
    worker: T
)(using context: EngineContext):

  def execute(
      processVariables: Seq[Either[BadVariableError, (String, Option[Json])]],
      generalVariables: GeneralVariables,
  ) =
    for {
      validatedInput <- InputValidator.validate(processVariables)
      initializedOutput <- Initializer.initVariables(validatedInput)
      proceedOrMocked <- OutMocker.mockOrProceed(
        generalVariables,
      )
      output <- worker.runWork(validatedInput, proceedOrMocked)
    } yield proceedOrMocked

  object InputValidator:
    lazy val prototype = worker.in
    lazy val customValidator = worker.customValidator

    def validate(inputParamsAsJson: Seq[Either[Any, (String, Option[Json])]]): Either[ValidatorError, In] =
      val jsonResult: Either[ValidatorError, Seq[(String, Option[Json])]] =
        inputParamsAsJson
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
                  .mkString("Validator Error(s):\n - ", " - ", "\n")
              )
            )
      val json: Either[ValidatorError, JsonObject] = jsonResult
        .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
          println(
            s" - $jsonKey: ${jsonValue.getClass.getSimpleName} - $jsonValue"
          )
          jsonObj.add(jsonKey, jsonValue.getOrElse(Json.Null))
        })
      json
        .flatMap(jsonObj =>
          decodeTo[In](jsonObj.asJson.toString).left
            .map(ex => ValidatorError(errorMsg = ex.errorMsg))
            .flatMap(in => customValidator.map(v => v(in)).getOrElse(Right(in)))
        )
    end validate

  end InputValidator

  object Initializer:
    private val defaultVariables = Map(
      "serviceName" -> "NOT-USED" // serviceName is not needed anymore
    )

    def initVariables(
        validatedInput: In
    ): Either[InitializerError, Map[String, Any]] = worker.variablesInit
      .map { vi =>
        vi(validatedInput).map(_ ++ defaultVariables)
      }
      .getOrElse(Right(defaultVariables))
  end Initializer

  object OutMocker:

    def mockOrProceed(
                       generalVariables: GeneralVariables,
                     ): Either[MockerError | MockedOutput, Option[Out]] =
      ((generalVariables.servicesMocked, generalVariables.isMocked(worker.topic), generalVariables.outputMockOpt) match
        case (_, _, Some(outputMock)) => // if the outputMock is set than we mock
          decodeMock(outputMock)
        case (_, true, _)
          if !isService => // if your process is NOT a Service check if it is mocked
          worker.defaultMock
        case (true, _, _)
          if isService => // if your process is a Service check if it is mocked
          worker.defaultMock
        case (_, _, None) =>
          Right(None)
        )

    end mockOrProceed

    private lazy val isService = worker.isInstanceOf[ServiceWorker[?, ?, ?, ?]]

    private def decodeMock(
                                                    json: Json,
                                                  ): Either[MockerError | MockedOutput, Option[Out]] =
      (json.isObject, isService) match
        case (true, true) =>
          decodeTo[Out](json.asJson.toString)
            .map(Some(_))
            .left
            .map(ex => MockerError(errorMsg = ex.errorMsg))
        case (true, _) =>
          Left(
            MockedOutput(output =
              context.toEngineObject(json)
            )
          )
        case _ =>
          Left(
            MockerError(errorMsg =
              s"The mock must be a Json Object:\n- $json\n- ${json.getClass}"
            )
          )
    end decodeMock

  end OutMocker

end WorkerExecutor
