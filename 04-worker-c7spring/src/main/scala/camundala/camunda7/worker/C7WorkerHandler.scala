package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import jakarta.annotation.PostConstruct
import org.camunda.bpm.client.{ExternalTaskClient, task, task as camunda}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import zio.{IO, Runtime, Unsafe, ZIO}
import zio.ZIO.*

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

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
  ): HelperContext[ZIO[Any, Throwable, Unit]] =
    val tryProcessVariables =
      ProcessVariablesExtractor.extract(worker.variableNames)
    val tryGeneralVariables = ProcessVariablesExtractor.extractGeneral()
    (for
        generalVariables      <- tryGeneralVariables
        given EngineRunContext = EngineRunContext(engineContext, generalVariables)
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
    ): HelperContext[Unit] =
      externalTaskService.complete(
        summon[camunda.ExternalTask],
        if manualOutMapping then Map.empty.asJava else filteredOutput.asJava, // Process Variables
        if !manualOutMapping then Map.empty.asJava else filteredOutput.asJava // local Variables
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
          logger.error(err)
          externalTaskService.handleFailure(
            summon[camunda.ExternalTask],
            err.causeMsg,
            s" ${err.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            0,
            0
          ) // TODO implement retry mechanism
    end handleError

  end extension

  private def filteredOutput(
      outputVariables: Seq[String],
      allOutputs: Map[String, Any]
  ): Map[String, Any] =
    outputVariables match
      case filter if filter.isEmpty => allOutputs
      case filter                   =>
        allOutputs
          .filter { case k -> _ => filter.contains(k) }

  end filteredOutput

  protected lazy val logger: WorkerLogger =
    engineContext.getLogger(classOf[C7WorkerHandler[?, ?]])

  private[worker] def isErrorHandled(error: CamundalaWorkerError, handledErrors: Seq[String]) =
    error.isMock || // if it is mocked, it is handled in the error, as it also could be a successful output
      handledErrors.contains(error.errorCode.toString) || handledErrors.map(
        _.toLowerCase
      ).contains("catchall")

  private lazy val runtime = Runtime.default

end C7WorkerHandler
