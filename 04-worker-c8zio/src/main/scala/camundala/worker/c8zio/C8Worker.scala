package camundala.worker.c8zio

import camundala.bpmn.GeneralVariables
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.*
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.{JobClient, JobHandler}
import zio.*
import zio.ZIO.*

import java.time
import scala.jdk.CollectionConverters.*
import java.util.Date

trait C8Worker[In: InOutDecoder, Out: InOutEncoder] extends JobWorker, JobHandler:
  protected def c8Context: C8Context
  private lazy val runtime = Runtime.default

  def handle(client: JobClient, job: ActivatedJob): Unit =
    Unsafe.unsafe:
      implicit unsafe =>
        runtime.unsafe.runToFuture(
          run(client, job)
            .provideLayer(ZioLogger.logger)
        ).future

  def run(client: JobClient, job: ActivatedJob): ZIO[Any, Throwable, Unit] =
    (for
      startDate        <- succeed(new Date())
      json             <- extractJson(job)
      businessKey      <- extractBusinessKey(json)
      _                <- logInfo(
                            s"Worker: ${job.getType} (${job.getWorker}) started > $businessKey"
                          )
      processVariables <- ZIO.foreach(worker.variableNames)(k => processVariable(k, json))
      generalVariables <- extractGeneralVariables(json)
      context           = EngineRunContext(c8Context, generalVariables)
      filteredOut      <- ZIO.fromEither(worker.executor(using context).execute(processVariables))
      _                <- logInfo(s"generalVariables: $generalVariables")
      _                <- handleSuccess(client, job, filteredOut, generalVariables.manualOutMapping, businessKey)
      _                <-
        logInfo(
          s"Worker: ${job.getType} (${job.getWorker}) ended ${printTimeOnConsole(startDate)} > $businessKey"
        )
    yield ())
      .catchAll: ex =>
        handleError(client, job, ex)

  private def handleSuccess(
      client: JobClient,
      job: ActivatedJob,
      filteredOutput: Map[String, Any],
      manualOutMapping: Boolean, // TODO no local variables?!
      businessKey: String
  ) =
    attempt(client.newCompleteCommand(job)
      .variables(filteredOutput.asJava)
      .send().join())
      .mapError(ex =>
        UnexpectedError(
          s"Problem complete job ${job.getKey} > $businessKey\n" + ex.getMessage
        )
      )

  private[worker] def handleError(
      client: JobClient,
      job: ActivatedJob,
      error: CamundalaWorkerError
  ): ZIO[Any, Throwable, Unit] =
    (for
      _ <- logError(s"Error: ${error.causeMsg}")
      json             <- extractJson(job)
      generalVariables <- extractGeneralVariables(json)
      isErrorHandled      = errorHandled(error, generalVariables.handledErrors)
      errorRegexHandled = regexMatchesAll(isErrorHandled, error, generalVariables.regexHandledErrors)
        _ <- attempt(client.newFailCommand(job)
             .retries(job.getRetries - 1)
             .retryBackoff(time.Duration.ofSeconds(60))
             .variables(Map("errorCode" -> error.errorCode, "errorMsg" -> error.errorMsg).asJava)
             .errorMessage(error.causeMsg)
             .send().join())
    yield (isErrorHandled, errorRegexHandled, generalVariables))
      .flatMap :
        case (true, true, generalVariables) =>
          val mockedOutput = error match
            case error: ErrorWithOutput =>
              error.output
            case _ => Map.empty
          val filtered = filteredOutput(generalVariables.outputVariables, mockedOutput)
          ZIO.attempt(
            if
              error.isMock && !generalVariables.handledErrors.contains(
                error.errorCode.toString
              )
            then
              handleSuccess(client, job, filtered, generalVariables.manualOutMapping, "")
            else
              val errorVars = Map(
                "errorCode" -> error.errorCode,
                "errorMsg" -> error.errorMsg
              )
              val variables = (filtered ++ errorVars).asJava
              client.newFailCommand(job)
                .retries(job.getRetries - 1)
                .retryBackoff(time.Duration.ofSeconds(60))
                .variables(variables)
                .errorMessage(error.causeMsg)
                .send().join()
          )
        case (true, false, _) =>
          ZIO.fail(HandledRegexNotMatchedError(error))
        case _ =>
          ZIO.fail(error)

  private def extractGeneralVariables(json: Json) =
    fromEither(
      customDecodeAccumulating[GeneralVariables](json.hcursor)
    ).mapError(ex =>
      ValidatorError(
        s"Problem extract general variables from $json\n" + ex.getMessage
      )
    )

  private def extractBusinessKey(json: Json) =
    fromEither(json.as[BusinessKey].map(_.businessKey.getOrElse("no businessKey")))
      .mapError(ex =>
        ValidatorError(
          s"Problem extract business Key from $json\n" + ex.getMessage
        )
      )

  private def extractJson(job: ActivatedJob) =
    fromEither(io.circe.parser.parse(job.getVariables))
      .mapError(ex =>
        ValidatorError(
          s"Problem Json Parsing process variables ${job.getVariables}\n" + ex.getMessage
        )
      )

  private def processVariable(
      key: String,
      json: Json
  ): UIO[Either[BadVariableError, (String, Option[Json])]] =
    ZIO.succeed(
      json.hcursor.downField(key).as[Option[Json]] match
        case Right(value) => Right(key -> value)
        case Left(ex)     => Left(BadVariableError(ex.getMessage))
    )

  case class BusinessKey(businessKey: Option[String])
  object BusinessKey:
    given InOutCodec[BusinessKey] = deriveInOutCodec

end C8Worker
