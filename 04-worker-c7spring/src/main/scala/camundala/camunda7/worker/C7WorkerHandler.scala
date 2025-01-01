package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import jakarta.annotation.PostConstruct
import org.camunda.bpm.client.{ExternalTaskClient, task as camunda}
import org.springframework.beans.factory.annotation.{Autowired, Value}

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

/** To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
  * parameters.
  */
trait C7WorkerHandler extends camunda.ExternalTaskHandler:

  @Value("${spring.application.name}")
  var applicationName: String = scala.compiletime.uninitialized

  @Autowired()
  protected var engineContext: EngineContext = scala.compiletime.uninitialized

  @Autowired()
  protected var externalTaskClient: ExternalTaskClient = scala.compiletime.uninitialized

  def worker: Worker[?, ?, ?]
  def topic: String

  override def execute(
      externalTask: camunda.ExternalTask,
      externalTaskService: camunda.ExternalTaskService
  ): Unit =
    Future:
      val startDate = new Date()
      logger.info(
        s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) started > ${externalTask.getBusinessKey}"
      )
      executeWorker(externalTaskService)(using externalTask)
      logger.info(
        s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) ended ${printTime(startDate)}   > ${externalTask.getBusinessKey}"
      )
  end execute

  @PostConstruct
  def registerHandler(): Unit =
    val appPackageName = applicationName.replace("-", ".")
    val testMode = sys.env.get("WORKER_TEST_MODE").contains("true") // did not work with lazy val
    if testMode || getClass.getName.startsWith(appPackageName)
    then
      externalTaskClient
        .subscribe(topic)
        .handler(this)
        .open()
      logger.info(s"Worker registered: $topic -> ${worker.getClass.getSimpleName}")
      logger.debug(prettyString(worker))
    else
      logger.info(
        s"Worker NOT registered: $topic -> ${worker.getClass.getSimpleName} (class starts not with $appPackageName)"
      )
    end if
  end registerHandler

  private def executeWorker(
      externalTaskService: camunda.ExternalTaskService
  ): HelperContext[Unit] =
    val tryProcessVariables =
      ProcessVariablesExtractor.extract(worker.variableNames ++ worker.inConfigVariableNames)
    val tryGeneralVariables = ProcessVariablesExtractor.extractGeneral()
    try
      (for
          generalVariables <- tryGeneralVariables
          context = EngineRunContext(engineContext, generalVariables)
          filteredOut <-
            worker.executor(using context).execute(tryProcessVariables)
        yield externalTaskService.handleSuccess(filteredOut, generalVariables.manualOutMapping) //
      ).left.map { ex =>
        externalTaskService.handleError(ex, tryGeneralVariables)
      }
    catch // safety net
      case ex: Throwable =>
        ex.printStackTrace()
        externalTaskService.handleError(
          UnexpectedError(errorMsg =
            s"We caught an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
          ),
          tryGeneralVariables
        )
    end try
  end executeWorker

  private def printTime(start: Date) =
    val time = new Date().getTime - start.getTime
    val color = if time > 1000 then Console.YELLOW_B
    else if time > 250 then Console.MAGENTA
    else Console.BLACK
    s"($color$time ms${Console.RESET})"
  end printTime

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
        tryGeneralVariables: Either[BadVariableError, GeneralVariables]
    ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      val errorMsg = error.errorMsg.replace("\n", "")
      (for
        generalVariables <- tryGeneralVariables
        errorHandled = isErrorHandled(error, generalVariables.handledErrors)
        errorRegexHandled = errorHandled && generalVariables.regexHandledErrors.forall(regex =>
          errorMsg.matches(s".*$regex.*")
        )
      yield (errorHandled, errorRegexHandled, generalVariables))
        .flatMap {
          case (true, true, generalVariables) =>
            val mockedOutput = error match
              case error: ErrorWithOutput =>
                error.output
              case _ => Map.empty
            val filtered = filteredOutput(generalVariables.outputVariables, mockedOutput)
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
                  "errorMsg" -> error.errorMsg
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
          case (true, false, _) =>
            Left(HandledRegexNotMatchedError(error))
          case _ =>
            Left(error)
        }
        .left
        .map { err =>
          logger.error(err)
          externalTaskService.handleFailure(
            summon[camunda.ExternalTask],
            err.causeMsg,
            s" ${err.causeMsg}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            0,
            0
          ) // TODO implement retry mechanism
        }
    end handleError

  end extension

  private def filteredOutput(
      outputVariables: Seq[String],
      allOutputs: Map[String, Any]
  ): Map[String, Any] =
    outputVariables match
      case filter if filter.isEmpty => allOutputs
      case filter =>
        allOutputs
          .filter { case k -> _ => filter.contains(k) }

  end filteredOutput

  protected lazy val logger: WorkerLogger =
    engineContext.getLogger(classOf[C7WorkerHandler])

  private[worker] def isErrorHandled(error: CamundalaWorkerError, handledErrors: Seq[String]) =
    error.isMock || // if it is mocked, it is handled in the error, as it also could be a successful output
      handledErrors.contains(error.errorCode.toString) || handledErrors.map(
        _.toLowerCase
      ).contains("catchall")

end C7WorkerHandler
