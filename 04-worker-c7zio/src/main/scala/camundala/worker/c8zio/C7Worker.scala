package camundala.worker.c8zio

import camundala.bpmn.GeneralVariables
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.camunda.bpm.client.task as camunda
import zio.*

import zio.ZIO.*

import java.util.Date
import scala.jdk.CollectionConverters.*

trait C7Worker[In <: Product: InOutCodec, Out <: Product: InOutCodec]
    extends WorkerDsl[In, Out], camunda.ExternalTaskHandler:

  protected def c7Context: C7Context
  
  def logger: WorkerLogger = Slf4JLogger.logger(getClass.getName)

  override def execute(
      externalTask: camunda.ExternalTask,
      externalTaskService: camunda.ExternalTaskService
  ): Unit =
    Unsafe.unsafe:
      implicit unsafe =>
        runtime.unsafe.runToFuture(
          run(externalTaskService)(using externalTask)
            .provideLayer(ZioLogger.logger)
        ).future

  private def run(externalTaskService: camunda.ExternalTaskService)(using
      externalTask: camunda.ExternalTask
  ): ZIO[Any, Throwable, Unit] =
    for
      startDate <- succeed(new Date())
      _         <-
        logInfo(
          s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) started > ${externalTask.getBusinessKey}"
        )
      _         <- executeWorker(externalTaskService)
      _         <-
        logInfo(
          s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) ended ${printTimeOnConsole(startDate)}   > ${externalTask.getBusinessKey}"
        )
    yield ()

  private def executeWorker(
      externalTaskService: camunda.ExternalTaskService
  ): HelperContext[ZIO[Any, Throwable, Unit]] =
    val tryProcessVariables =
      ProcessVariablesExtractor.extract(worker.variableNames)
    val tryGeneralVariables = ProcessVariablesExtractor.extractGeneral()
    (for
        generalVariables <- tryGeneralVariables
        context           = EngineRunContext(c7Context, generalVariables)
        filteredOut      <-
          ZIO.fromEither(
            worker.executor(using context).execute(variablesAsEithers(tryProcessVariables))
          )
        _                <- ZIO.attempt(externalTaskService.handleSuccess(
                              filteredOut,
                              generalVariables.manualOutMapping
                            ))
      yield () //
    ).mapError:
      case ex: CamundalaWorkerError => ex
      case ex                       => UnexpectedError(ex.getMessage)
    .mapError: ex =>
      externalTaskService.handleError(ex, tryGeneralVariables)
      ex
  end executeWorker

  private def variablesAsEithers(tryProcessVariables: Seq[IO[
    BadVariableError,
    (String, Option[Json])
  ]]): Seq[Either[BadVariableError, (String, Option[Json])]] =
    tryProcessVariables
      .map((x: IO[BadVariableError, (String, Option[Json])]) =>
        Unsafe.unsafe:
          implicit unsafe => // can be removed if everything is ZIO
            runtime.unsafe.run(x.either).getOrThrow()
      )

  extension (externalTaskService: camunda.ExternalTaskService)

    private def handleSuccess(
        filteredOutput: Map[String, Any],
        manualOutMapping: Boolean
    ): HelperContext[Unit] =
      externalTaskService.complete(
        summon[camunda.ExternalTask],
        if manualOutMapping then Map.empty.asJava else filteredOutput.asJava, // Process Variables
        if !manualOutMapping then Map.empty.asJava else filteredOutput.asJava // local Variables
      )

    private[worker] def handleError(
        error: CamundalaWorkerError,
        tryGeneralVariables: IO[BadVariableError, GeneralVariables]
    ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      val errorMsg = error.errorMsg.replace("\n", "")
      (for
        generalVariables <- tryGeneralVariables
        isErrorHandled    = errorHandled(error, generalVariables.handledErrors)
        errorRegexHandled =
          regexMatchesAll(isErrorHandled, error, generalVariables.regexHandledErrors)
      yield (isErrorHandled, errorRegexHandled, generalVariables))
        .flatMap {
          case (true, true, generalVariables) =>
            val mockedOutput = error match
              case error: ErrorWithOutput =>
                error.output
              case _                      => Map.empty
            val filtered     = filteredOutput(generalVariables.outputVariables, mockedOutput)
            ZIO.succeed(
              if
                error.isMock && !generalVariables.handledErrors.contains(
                  error.errorCode.toString
                )
              then
                handleSuccess(filtered, generalVariables.manualOutMapping)
              else
                val errorVars = Map(
                  "errorCode" -> error.errorCode,
                  "errorMsg"  -> error.errorMsg
                )
                val variables = (filtered ++ errorVars).asJava
                logger.info(s"Handled Error: $errorVars")
                externalTaskService.handleBpmnError(
                  summon[camunda.ExternalTask],
                  s"${error.errorCode}",
                  error.errorMsg,
                  variables
                )
            )
          case (true, false, _)               =>
            ZIO.fail(HandledRegexNotMatchedError(error))
          case _                              =>
            ZIO.fail(error)
        }
        .mapError: err =>
          logger.error(err)
          externalTaskService.handleFailure(
            summon[camunda.ExternalTask],
            err.causeMsg,
            s" ${err.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            summon[camunda.ExternalTask].getRetries - 1,
            timeout.toMillis
          )

    end handleError

  end extension
  private lazy val runtime = Runtime.default

end C7Worker
