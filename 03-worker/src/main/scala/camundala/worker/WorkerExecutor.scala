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
      validatedInput               <- InputValidator.validate(processVariables)
      initializedOutput            <- Initializer.initVariables(validatedInput)
      mockedOutput                 <- OutMocker(worker).mockedOutput(validatedInput)
      // only run the work if it is not mocked
      output                       <-
        if mockedOutput.isEmpty then WorkRunner(worker).run(validatedInput)
        else ZIO.succeed(mockedOutput.get)
      allOutputs: Map[String, Any]  = camundaOutputs(validatedInput, initializedOutput, output)
      filteredOut: Map[String, Any] =
        filteredOutput(allOutputs, context.generalVariables.outputVariables)
      // make MockedOutput as error if mocked
      _                            <- if mockedOutput.isDefined then ZIO.fail(MockedOutput(filteredOut)) else ZIO.succeed(())
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
                ZIO.fromEither(validationHandler.validate(in))
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
        .getOrElse:
          ZIO.succeed(defaultVariables)
  end Initializer

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
          context.toEngineObject(output.asInstanceOf[Out]))

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
