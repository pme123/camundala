package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.bpmn.WithConfig
import io.circe.syntax.*

case class WorkerExecutor[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
](
    worker: T
)(using context: EngineRunContext):

  def execute(
      processVariables: Seq[Either[BadVariableError, (String, Option[Json])]]
  ): Either[CamundalaWorkerError, Map[String, Any]] =
    for
      validatedInput    <- InputValidator.validate(processVariables)
      initializedOutput <- Initializer.initVariables(validatedInput)(using context.engineContext)
      mockedOutput      <- OutMocker.mockedOutput(validatedInput)
      // only run the work if it is not mocked
      output            <-
        if mockedOutput.isEmpty then WorkRunner.run(validatedInput) else Right(mockedOutput.get)
      allOutputs         = camundaOutputs(validatedInput, initializedOutput, output)
      filteredOut        = filteredOutput(allOutputs)
      // make MockedOutput as error if mocked
      _                 <- if mockedOutput.isDefined then Left(MockedOutput(filteredOut)) else Right(())
    yield filteredOut

  object InputValidator:
    lazy val prototype         = worker.in
    lazy val validationHandler = worker.validationHandler

    def validate(
        inputParamsAsJson: Seq[Either[Any, (String, Option[Json])]]
    ): Either[ValidatorError, In] =
      val jsonResult: Either[ValidatorError, Seq[(String, Option[Json])]]                  =
        inputParamsAsJson
          .partition(_.isRight) match
          case (successes, failures) if failures.isEmpty =>
            Right(
              successes.collect { case Right(value) => value }
            )
          case (_, failures)                             =>
            Left(
              ValidatorError(
                failures
                  .collect { case Left(value) => value }
                  .mkString("Validator Error(s):\n - ", " - ", "\n")
              )
            )
      val json: Either[ValidatorError, JsonObject]                                         = jsonResult
        .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
          if jsonValue.isDefined
          then jsonObj.add(jsonKey, jsonValue.get)
          else jsonObj
        })
      def toIn(posJsonObj: Either[ValidatorError, JsonObject]): Either[ValidatorError, In] =
        posJsonObj
          .flatMap(jsonObj =>
            decodeTo[In](jsonObj.asJson.deepDropNullValues.toString).left
              .map(ex => ValidatorError(errorMsg = ex.errorMsg))
              .flatMap(in => validationHandler.map(h => h.validate(in)).getOrElse(Right(in)))
          )

      val in     = toIn(json)
      val result = in.flatMap:
        case i: WithConfig[?] =>
          val newIn =
            for
              jsonObj: JsonObject   <- json
              inputVariables         = jsonObj.toMap
              configJson: JsonObject =
                inputVariables.get("inConfig").getOrElse(i.defaultConfigAsJson).asObject.get
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
    ): InitProcessFunction =
      worker.initProcessHandler
        .map { vi =>
          vi.init(validatedInput).map(_ ++ defaultVariables)
        }
        .getOrElse(Right(defaultVariables))
  end Initializer

  object OutMocker:

    def mockedOutput(in: In): Either[MockerError, Option[Out]] =
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
          Right(None)
    end mockedOutput

    private def decodeMock(
        json: Json
    ) =
      json.as[Out]
        .map:
          Some(_)
        .left.map: error =>
          MockerError(errorMsg = s"$error:\n- $json")
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
    context.toEngineObject(initializedInput) ++ internalVariables ++
      (output match
        case o: NoOutput =>
          context.toEngineObject(o)
        case _           =>
          context.toEngineObject(output.asInstanceOf[Out])
      )
  private def filteredOutput(
      allOutputs: Map[String, Any]
  ): Map[String, Any] =
    val filter = context.generalVariables.outputVariables
    if filter.isEmpty then
      allOutputs
    else
      allOutputs
        .filter { case k -> _ => filter.contains(k) }
    end if
  end filteredOutput

end WorkerExecutor
