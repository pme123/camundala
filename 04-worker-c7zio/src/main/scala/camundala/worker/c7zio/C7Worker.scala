package camundala.worker.c7zio

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

  def logger = Slf4JLogger.logger(getClass.getName)

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
  end execute

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
        generalVariables      <- tryGeneralVariables
        given EngineRunContext = EngineRunContext(c7Context, generalVariables)
        filteredOut           <- WorkerExecutor(worker).execute(tryProcessVariables)
        _                     <- ZIO.attempt(externalTaskService.handleSuccess(
                                   filteredOut,
                                   generalVariables.manualOutMapping
                                 ))
      yield () //
    ).mapError:
      case ex: CamundalaWorkerError => ex
      case ex                       => UnexpectedError(ex.getMessage)
    .catchAll: ex =>
      tryGeneralVariables.map: vars =>
        externalTaskService.handleError(ex, vars)
        ex
  end executeWorker

  extension (externalTaskService: camunda.ExternalTaskService)

    private def handleSuccess(
        filteredOutput: Map[String, Any],
        manualOutMapping: Boolean
    )(using externalTask: camunda.ExternalTask): ZIO[Any, UnexpectedError, Unit] =
      ZIO.attempt(
        externalTaskService.complete(
          summon[camunda.ExternalTask],
          if manualOutMapping then Map.empty.asJava else filteredOutput.asJava, // Process Variables
          if !manualOutMapping then Map.empty.asJava else filteredOutput.asJava // local Variables
        )
      ).mapError: err =>
        UnexpectedError(
          s"There is an unexpected Error from completing a successful Worker to C7: ${err.getMessage}."
        )

    private[worker] def handleError(
        error: CamundalaWorkerError,
        generalVariables: GeneralVariables
    ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      val errorMsg          = error.errorMsg.replace("\n", "")
      val errorHandled      = isErrorHandled(error, generalVariables.handledErrors)
      val errorRegexHandled = errorHandled && generalVariables.regexHandledErrors.forall(regex =>
        errorMsg.matches(s".*$regex.*")
      )
      ((errorHandled, errorRegexHandled, generalVariables) match
        case (true, true, generalVariables) =>
          val mockedOutput = error match
            case error: ErrorWithOutput =>
              error.output
            case _                      => Map.empty
          val filtered     = filteredOutput(generalVariables.outputVariables, mockedOutput)
          Right(
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
          Left(HandledRegexNotMatchedError(error))
        case _                              =>
          Left(error)
      )
        .left.map: err =>
          val taskId  = summon[camunda.ExternalTask].getId
          val retries =
            summon[camunda.ExternalTask].getRetries match
              case r if r <= 0 => 3
              case r           => r - 1
          if retries == 0 then logger.error(err)
          logger.info(s"Retries left for $taskId: $retries")
          externalTaskService.handleFailure(
            taskId,
            err.causeMsg,
            s" ${err.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            retries,
            timeout.toMillis
          )

    end handleError

    private[worker] def isErrorHandled(error: CamundalaWorkerError, handledErrors: Seq[String]) =
      error.isMock || // if it is mocked, it is handled in the error, as it also could be a successful output
        handledErrors.contains(error.errorCode.toString) || handledErrors.map(
          _.toLowerCase
        ).contains("catchall")

  end extension
  private lazy val runtime = Runtime.default

end C7Worker
