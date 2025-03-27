package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.syntax.*
import zio.*

case class WorkerExecutor[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
](
    worker: T
)(using context: EngineRunContext):
  given EngineContext = context.engineContext

  def execute(
      processVariables: Seq[IO[BadVariableError, (String, Option[Json])]]
  ): IO[CamundalaWorkerError, Map[String, Any]] =
    (for
      validatedInput    <- InputValidator.validate(processVariables)
      initializedOutput <- Initializer.initVariables(validatedInput)
      mockedOutput      <- OutMocker.mockedOutput(validatedInput)
      // only run the work if it is not mocked
      output            <-
        if mockedOutput.isEmpty then WorkRunner.run(validatedInput)
        else ZIO.succeed(mockedOutput.get)
      allOutputs: Map[String, Any] = camundaOutputs(validatedInput, initializedOutput, output)
      filteredOut: Map[String, Any] = filteredOutput(allOutputs, context.generalVariables.outputVariables)
      // make MockedOutput as error if mocked
      _                 <- if mockedOutput.isDefined then ZIO.fail(MockedOutput(filteredOut)) else ZIO.succeed(())
    yield filteredOut)

  object InputValidator:
    lazy val prototype         = worker.in
    lazy val validationHandler = worker.validationHandler

    def validate(
        inputParamsAsJson: Seq[IO[Any, (String, Option[Json])]]
    ): IO[ValidatorError, In] =

      val jsonResult: IO[ValidatorError, Seq[(String, Option[Json])]] =
        ZIO
          .partition(inputParamsAsJson)(i => i)
          .flatMap:
            case (failures, successes) if failures.isEmpty =>
              ZIO.succeed(successes.toSeq)
            case (failures, _)                             =>
              ZIO.fail(
                ValidatorError(
                  failures
                    .collect { case Left(value) => value }
                    .mkString("Validator Error(s):\n - ", " - ", "\n")
                )
              )

      val json: IO[ValidatorError, JsonObject] = jsonResult
        .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
          if jsonValue.isDefined
          then jsonObj.add(jsonKey, jsonValue.get)
          else jsonObj
        })

      def toIn(posJsonObj: IO[ValidatorError, JsonObject]): IO[ValidatorError, In] =
        posJsonObj
          .flatMap(jsonObj =>
            decodeTo[In](jsonObj.asJson.deepDropNullValues.toString)
              .mapError(ex => ValidatorError(errorMsg = ex.errorMsg))
              .flatMap(in =>
                validationHandler.map(h => ZIO.fromEither(h.validate(in))).getOrElse(ZIO.succeed(
                  in
                ))
              )
          )

      val in     = toIn(json)
      val result = in.flatMap:
        case i: WithConfig[?] =>
          val newIn =
            for
              jsonObj: JsonObject   <- json
              inputVariables         = jsonObj.toMap
              configJson: JsonObject =
                inputVariables.getOrElse("inConfig", i.defaultConfigAsJson).asObject.get
              newJsonConfig          = worker.inConfigVariableNames
                                         .foldLeft(configJson):
                                           case (configJson, n) =>
                                             if jsonObj.contains(n)
                                             then configJson.add(n, jsonObj(n).get)
                                             else configJson
            yield jsonObj.add("inConfig", newJsonConfig.asJson)
          toIn(newIn)

        case x =>
          in
      result
    end validate

  end InputValidator

  object Initializer:
    private val defaultVariables = Map(
      "serviceName" -> "NOT-USED" // serviceName is not needed anymore
    )

    def initVariables(
        validatedInput: In
    )(using EngineContext): IO[InitProcessError, Map[String, Any]] =
      worker.initProcessHandler
        .map: vi =>
          vi.init(validatedInput).map(_ ++ defaultVariables)
        .map:
          ZIO.fromEither
        .getOrElse:
          ZIO.succeed(defaultVariables)
  end Initializer

  object OutMocker:

    def mockedOutput(in: In): IO[MockerError, Option[Out]] =
      (
        context.generalVariables.isMockedWorker(worker.topic),
        context.generalVariables.outputMock,
        context.generalVariables.outputServiceMock
      ) match
        // if the outputMock is set than we mock
        case (_, Some(outputMock), _) =>
          decodeMock(outputMock)
        // if your worker is mocked we use the default mock
        case (true, None, None)       =>
          worker.defaultMock(in).map(Some(_))
        // otherwise it is not mocked or it is a service mock which is handled in service Worker during running
        case (_, None, _)             =>
          ZIO.succeed(None)
    end mockedOutput

    private def decodeMock(
        json: Json
    ) =
      ZIO.fromEither(json.as[Out])
        .map:
          Some(_)
        .mapError: error =>
          MockerError(errorMsg = s"$error:\n- $json")
    end decodeMock

  end OutMocker

  object WorkRunner:
    def run(inputObject: In)(using EngineRunContext): IO[RunWorkError, Out | NoOutput] =
      worker.runWorkHandler
        .map:
          _.runWorkZIO(inputObject)
        .getOrElse:
          ZIO.succeed(NoOutput())
  end WorkRunner

  private def camundaOutputs(
      initializedInput: In,
      internalVariables: Map[String, Any],
      output: Out | NoOutput
  )(using context: EngineRunContext): Map[String, Any] =
    context.toEngineObject(initializedInput) ++ internalVariables ++
      (output match
        case o: NoOutput =>
          context.toEngineObject(o)
        case _           =>
          context.toEngineObject(output.asInstanceOf[Out])
        )

  private def filteredOutput(
      allOutputs: Map[String, Any],
      outputVariables: Seq[String]
  ): Map[String, Any] =
    if outputVariables.isEmpty then
      allOutputs
    else
      allOutputs
        .filter { case k -> _ => outputVariables.contains(k) }
    end if
  end filteredOutput

end WorkerExecutor
