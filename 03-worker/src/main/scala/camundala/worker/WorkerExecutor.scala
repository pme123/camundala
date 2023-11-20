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
)(using context: EngineRunContext):

  def execute(
      processVariables: Seq[Either[BadVariableError, (String, Option[Json])]]
  ) =
    for {
      validatedInput <- InputValidator.validate(processVariables)
      initializedOutput <- Initializer.initVariables(validatedInput)
      _ <- OutMocker.mockOrProceed()
      output <- WorkRunner.run(validatedInput)
      allOutputs = camundaOutputs(validatedInput, initializedOutput, output)
      filteredOut = filteredOutput(allOutputs)
    } yield filteredOut

  object InputValidator:
    lazy val prototype = worker.in
    lazy val validationHandler = worker.validationHandler

    def validate(
        inputParamsAsJson: Seq[Either[Any, (String, Option[Json])]]
    ): Either[ValidatorError, In] =
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
          jsonObj.add(jsonKey, jsonValue.getOrElse(Json.Null))
        })
      json
        .flatMap(jsonObj =>
          decodeTo[In](jsonObj.asJson.toString).left
            .map(ex => ValidatorError(errorMsg = ex.errorMsg))
            .flatMap(in => validationHandler.map(h => h.validate(in)).getOrElse(Right(in)))
        )
    end validate

  end InputValidator

  object Initializer:
    private val defaultVariables = Map(
      "serviceName" -> "NOT-USED" // serviceName is not needed anymore
    )

    def initVariables(
        validatedInput: In
    ): Either[InitProcessError, Map[String, Any]] = worker.initProcessHandler
      .map { vi =>
        vi.init(validatedInput).map(_ ++ defaultVariables)
      }
      .getOrElse(Right(defaultVariables))
  end Initializer

  object OutMocker:

    def mockOrProceed(): Either[MockerError | MockedOutput, Option[Out]] =
      (
        context.generalVariables.defaultMocked,
        context.generalVariables.isMockedSubprocess(worker.topic),
        context.generalVariables.outputMockOpt,
        context.generalVariables.outputServiceMockOpt
      ) match
        case (_, _, Some(outputMock), _) => // if the outputMock is set than we mock
          Left(decodeMock(outputMock))
        case (_, true, _, None) => // if your process is a SubProcess check if it is mocked
          Left(worker.defaultMock)
        case (true, _, _, None)
            if defaultMocksAllowed => // if your process is a ExternalTask check if it is mocked
          Left(worker.defaultMock)
        case (_, _, None, _) =>
          Right(None)
    end mockOrProceed

    //TODO default mocking only for ServiceWorker possible - subprocesses and customworkers only with mockedSubprocesses possible and that is a bad name
    private lazy val defaultMocksAllowed = false //  worker.isInstanceOf[CustomWorker[?, ?]]

    private def decodeMock(
        json: Json
    )(using
        context: EngineRunContext
    ): MockerError | MockedOutput =
      json.isObject match
        case true =>
          MockedOutput(output = context.jsonObjectToEngineObject(json.asObject.get))
        case _ =>
          MockerError(errorMsg = s"The mock must be a Json Object:\n- $json\n- ${json.getClass}")
    end decodeMock

  end OutMocker

  object WorkRunner:
    def run(inputObject: In): Either[RunWorkError, Out | NoOutput] =
      worker.runWorkHandler
        .map(_.runWork(inputObject))
        .getOrElse(Right(NoOutput()))
  end WorkRunner

  private def camundaOutputs(
      initializedInput: In,
      internalVariables: Map[String, Any],
      output: Out | NoOutput
  ): Map[String, Any] =
    context.toEngineObject(initializedInput) ++ internalVariables ++ (output match
      case o: NoOutput =>
        context.toEngineObject(o)
      case o: Out =>
        context.toEngineObject(o)
    )
  private def filteredOutput(
      allOutputs: Map[String, Any]
  ): Map[String, Any] =
    val filter = context.generalVariables.outputVariables
    if (filter.isEmpty)
      allOutputs
    else
      allOutputs
        .filter { case k -> _ => filter.contains(k) }

end WorkerExecutor