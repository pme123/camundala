package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import jakarta.annotation.PostConstruct
import org.camunda.bpm.client.{ExternalTaskClient, task as camunda}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import zio.{URIO, Unsafe, ZIO}
import zio.ZIO.*

import scala.concurrent.duration.*
import java.util.Date
import scala.jdk.CollectionConverters.*

/** To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
  * parameters.
  */
trait C7WorkerHandler[In <: Product: InOutCodec, Out <: Product: InOutCodec]
    extends camunda.ExternalTaskHandler, WorkerHandler[In, Out]:

  @Value("${spring.application.name}")
  var applicationName: String = scala.compiletime.uninitialized

  @Autowired()
  protected var engineContext: EngineContext = scala.compiletime.uninitialized

  @Autowired()
  protected var externalTaskClient: ExternalTaskClient = scala.compiletime.uninitialized

  protected lazy val logger: WorkerLogger = engineContext.getLogger(getClass)

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

  @PostConstruct
  def registerHandler(): Unit =
    registerHandler:
      externalTaskClient
        .subscribe(topic)
        .handler(this)
        .open()
  end registerHandler

  private def executeWorker(
      externalTaskService: camunda.ExternalTaskService
  )(using et: camunda.ExternalTask): ZIO[Any, Throwable, Unit] =
    val tryProcessVariables =
      ProcessVariablesExtractor.extract(worker.variableNames)
    (for
        generalVariables      <- ProcessVariablesExtractor.extractGeneral()
        given EngineRunContext = EngineRunContext(engineContext, generalVariables)
        filteredOut           <- WorkerExecutor(worker).execute(tryProcessVariables)
        _                     <- externalTaskService.handleSuccess(
                                   filteredOut,
                                   generalVariables.manualOutMapping
                                 )
      yield () //
    ).flatMapError: ex =>
      ProcessVariablesExtractor.extractGeneral()
        .flatMap: generalVariables =>
          externalTaskService.handleError(ex, generalVariables)
        .fold(
          exc =>
            logger.error(exc)
            exc
          ,
          {
            case err: MockedOutput =>
              err
            case err               =>
              logger.error(err)
              err
          }
        )
    .fold(
      ex => (),
      _ => ()
    )

  end executeWorker

  extension (externalTaskService: camunda.ExternalTaskService)

    private[worker] def handleSuccess(
        filteredOutput: Map[String, Any],
        manualOutMapping: Boolean
    )(using externalTask: camunda.ExternalTask): URIO[Any, Unit] =
      ZIO.attempt(
        externalTaskService.complete(
          summon[camunda.ExternalTask],
          if manualOutMapping then Map.empty.asJava else filteredOutput.asJava, // Process Variables
          if !manualOutMapping then Map.empty.asJava else filteredOutput.asJava // local Variables
        )
      ).flatMapError: err =>
        handleFailure(
          UnexpectedError(
            s"There is an unexpected Error from completing a successful Worker to C7: ${err.getMessage}."
          )
        )
      .fold(
        err => logger.error(err),
        _ => ()
      )

    private[worker] def handleError(
        error: CamundalaWorkerError,
        generalVariables: GeneralVariables
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      checkError(error, generalVariables)
        .flatMap:
          case err: (UnexpectedError | MockedOutput) =>
            ZIO.succeed(err)
          case err                                   =>
            handleFailure(err, doRetry = true)

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
          (if
             error.isMock && !generalVariables.handledErrors.contains(
               error.errorCode.toString
             )
           then
             handleSuccess(filtered, generalVariables.manualOutMapping)
           else
             handleBpmnError(error, filtered)
          )
            .fold(
              ex => ex,
              _ => error
            )
        case (true, false) =>
          ZIO.succeed(HandledRegexNotMatchedError(error))
        case _             =>
          ZIO.succeed(error)
      end match
    end checkError

    private[worker] def handleBpmnError(
        error: CamundalaWorkerError,
        filteredGeneralVariables: Map[String, Any]
    ): HelperContext[ZIO[Any, CamundalaWorkerError, Unit]] =
      val errorVars = Map(
        "errorCode" -> error.errorCode,
        "errorMsg"  -> error.errorMsg
      )
      val variables = (filteredGeneralVariables ++ errorVars).asJava
      ZIO.attempt(
        externalTaskService.handleBpmnError(
          summon[camunda.ExternalTask],
          s"${error.errorCode}",
          error.errorMsg,
          variables
        )
      ).flatMapError: err =>
        handleFailure(
          UnexpectedError(s"Problem handling BpmnError to C7: ${err.getMessage}.")
        )
    end handleBpmnError

    private[worker] def handleFailure(
        error: CamundalaWorkerError,
        doRetry: Boolean = false
    ): HelperContext[URIO[Any, CamundalaWorkerError]] =
      val taskId      = summon[camunda.ExternalTask].getId
      val businessKey = summon[camunda.ExternalTask].getBusinessKey
      val retries     = calcRetries(error)

      if retries == 0 then logger.error(error)
      logger.info(s"Retries left for $taskId ($businessKey): $retries - $error")
      if retries >= 0 || doRetry then
        ZIO.attempt(
          externalTaskService.handleFailure(
            taskId,
            error.causeMsg,
            s" ${error.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            Math.max(retries, 0), // < 0 not allowed
            10.seconds.toMillis
          )
        ).flatMapError: throwable =>
          handleFailure(
            UnexpectedError(s"Problem handling Failure to C7: ${throwable.getMessage}.")
          )
        .fold(
          err =>
            logger.warn(s"ERROR fold error: ${err.errorMsg}")
            err
          ,
          s =>
            error
        )
      else
        ZIO.succeed(error)
      end if
    end handleFailure

    private[worker] def calcRetries(
        error: CamundalaWorkerError
    ): HelperContext[Int] =
      val doRetryMsgs = Seq(
        "Entity was updated by another transaction concurrently",
        "An exception occurred in the persistence layer"
      )
      val doRetry     = doRetryMsgs.exists(error.errorMsg.contains)

      summon[camunda.ExternalTask].getRetries match
        case r if r <= 0 && doRetry => 3
        case r                      => r - 1

    end calcRetries
    private def filteredOutput(
        outputVariables: Seq[String],
        allOutputs: Map[String, Any]
    ): Map[String, Any] =
      outputVariables match
        case filter if filter.isEmpty => allOutputs
        case filter                   =>
          allOutputs
            .filter:
              case k -> _ => filter.contains(k)

    end filteredOutput

  end extension
  private lazy val runtime = zio.Runtime.default

end C7WorkerHandler
