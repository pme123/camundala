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
  )(using camunda.ExternalTask): ZIO[Any, Throwable, Unit] =
    val tryProcessVariables =
      ProcessVariablesExtractor.extract(worker.variableNames)
    (for
        generalVariables      <- ProcessVariablesExtractor.extractGeneral()
        given EngineRunContext = EngineRunContext(c7Context, generalVariables)
        filteredOut           <- WorkerExecutor(worker).execute(tryProcessVariables)
        _                     <- externalTaskService.handleSuccess(
                                   filteredOut,
                                   generalVariables.manualOutMapping
                                 )
      yield () //
    ).mapError:
      case ex: CamundalaWorkerError =>
        ex
      case ex                       =>
        UnexpectedError(ex.getMessage)
    .flatMapError: ex =>
      ProcessVariablesExtractor.extractGeneral()
        .flatMap: generalVariables =>
          externalTaskService.handleError(ex, generalVariables)
        .fold(
          exc => exc,
          err => err
        )
    .mapError:
      case err: UnexpectedError =>
        err.printStackTrace()
        err

  end executeWorker

  extension (externalTaskService: camunda.ExternalTaskService)

    private[worker] def handleSuccess(
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
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      checkError(error, generalVariables)
        .flatMap:
          case err: UnexpectedError =>
            ZIO.succeed(err)
          case err                  =>
            handleFailure(err)

    end handleError

    private[worker] def isErrorHandled(
        error: CamundalaWorkerError,
        handledErrors: Seq[String]
    ): Boolean =
      error.isMock || // if it is mocked, it is handled in the error, as it also could be a successful output
        handledErrors.contains(error.errorCode.toString) ||
        handledErrors.map(
          _.toLowerCase
        ).contains("catchall")

    private[worker] def checkError(
        error: CamundalaWorkerError,
        generalVariables: GeneralVariables
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      val errorMsg          = error.errorMsg.replace("\n", "")
      val errorHandled      = isErrorHandled(error, generalVariables.handledErrors)
      val errorRegexHandled =
        errorHandled && generalVariables.regexHandledErrors.forall(regex =>
          errorMsg.matches(s".*$regex.*")
        )
      (errorHandled, errorRegexHandled) match
        case (true, true)  =>
          val mockedOutput               = error match
            case error: ErrorWithOutput =>
              error.output
            case _                      => Map.empty
          val filtered: Map[String, Any] =
            filteredOutput(generalVariables.outputVariables, mockedOutput)
          if
            error.isMock && !generalVariables.handledErrors.contains(
              error.errorCode.toString
            )
          then
            handleSuccess(filtered, generalVariables.manualOutMapping)
              .fold(
                ex => ex,
                _ => error
              )
          else
            handleBpmnError(error, filtered)
          end if
        case (true, false) =>
          ZIO.succeed(HandledRegexNotMatchedError(error))
        case _             =>
          ZIO.succeed(error)
      end match
    end checkError

    private[worker] def handleFailure(
        error: CamundalaWorkerError
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      val taskId  = summon[camunda.ExternalTask].getId
      val retries =
        if error.isInstanceOf[CamundalaWorkerError.ServiceError]
        then
          summon[camunda.ExternalTask].getRetries match
            case r if r <= 0 => 3
            case r           => r - 1
        else 0

      if retries == 0 then logger.error(error)
      logger.info(s"Retries left for $taskId: $retries")
      ZIO.attempt(
        externalTaskService.handleFailure(
          taskId,
          error.causeMsg,
          s" ${error.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
          retries,
          timeout.toMillis
        )
      ).fold(
        ex =>
          UnexpectedError(s"Problem handling Failure to C7: ${ex.getMessage}."),
        s => error
      )
    end handleFailure

    private[worker] def handleBpmnError(
        error: CamundalaWorkerError,
        filteredGeneralVariables: Map[String, Any]
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      val errorVars = Map(
        "errorCode" -> error.errorCode,
        "errorMsg"  -> error.errorMsg
      )
      val variables = (filteredGeneralVariables ++ errorVars).asJava
      logger.info(s"Handled Error: $errorVars")
      ZIO.attempt(
        externalTaskService.handleBpmnError(
          summon[camunda.ExternalTask],
          s"${error.errorCode}",
          error.errorMsg,
          variables
        )
      ).fold(
        err => UnexpectedError(s"Problem handling BpmnError to C7: ${err.getMessage}."),
        _ => error
      )
    end handleBpmnError

  end extension
  private lazy val runtime = Runtime.default

end C7Worker
