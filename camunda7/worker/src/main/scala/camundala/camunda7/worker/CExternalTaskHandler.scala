package camundala
package camunda7.worker

import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.camunda.bpm.client.task as camunda
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.*

/**
 * To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
 * parameters.
 */
trait CExternalTaskHandler extends camunda.ExternalTaskHandler, CamundaHelper:

  def engineContext: EngineContext
  def topic: String
  def worker: Worker[?,?,?]
  def variableNames: Seq[String] = worker.variableNames

  override def execute(
                        externalTask: camunda.ExternalTask,
                        externalTaskService: camunda.ExternalTaskService
                      ): Unit =
    logger.info(
      s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) started > ${externalTask.getBusinessKey}"
    )
    executeWorker(externalTaskService)(using externalTask)
    logger.info(
      s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) ended   > ${externalTask.getBusinessKey}"
    )
  end execute

  private def executeWorker(
                             externalTaskService: camunda.ExternalTaskService
                           ): HelperContext[Unit] =
    val tryProcessVariables = ProcessVariablesExtractor.extract(variableNames)
    val tryGeneralVariables = ProcessVariablesExtractor.extractGeneral()
    try {
      (for {
        generalVariables <- tryGeneralVariables
        c = engineContext
        context = EngineRunContext(engineContext, generalVariables)
        filteredOut <- worker.executor(using context).execute(tryProcessVariables)
      } yield externalTaskService.handleSuccess(filteredOut) //
        ).left.map { ex =>
        externalTaskService.handleError(ex, tryGeneralVariables)
      }
    } catch { // safety net
      case ex: Throwable =>
        ex.printStackTrace()
       externalTaskService.handleError(
          UnexpectedError(errorMsg =
            s"We caught an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
          ),
         tryGeneralVariables
        )
    }
  end executeWorker

  extension (externalTaskService: camunda.ExternalTaskService)

    private def handleSuccess(
                               filteredOutput: Map[String, Any]
                             ): HelperContext[Unit] =
      externalTaskService.complete(
        summon[camunda.ExternalTask],
        filteredOutput.asJava,
        Map.empty.asJava
      )

    private def handleError(
                             error: CamundalaWorkerError,
                             tryGeneralVariables: Either[BadVariableError, GeneralVariables]
                           ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      (for {
        generalVariables <- tryGeneralVariables
        errorHandled = error.isMock || generalVariables.handledErrors.contains(
          error.errorCode.toString
        )
        errorRegexHandled = errorHandled && generalVariables.regexHandledErrors.forall(regex =>
          error.errorMsg.matches(s".*$regex.*")
        )
      } yield (errorHandled, errorRegexHandled, generalVariables))
        .flatMap {
          case (true, true, generalVariables) =>
            val mockedOutput = error match
              case error: ErrorWithOutput =>
                error.output
              case _ => Map.empty
            val filtered = filteredOutput(generalVariables.outputVariables, mockedOutput)
            Right(externalTaskService.handleBpmnError(
              summon[camunda.ExternalTask],
              s"${error.errorCode}",
              error.errorMsg,
              filtered.asJava
            ))
          case (true, false, _) =>
            Left(HandledRegexNotMatchedError(error))
          case _ =>
            Left(error)
        }
        .left
        .map { err =>
          val errMessage = s"${err.errorCode}: ${err.errorMsg}"
          externalTaskService.handleFailure(
            summon[camunda.ExternalTask],
            errMessage,
            s" $errMessage\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            0,
            0
          ) //TODO implement retry mechanism
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
    Camunda7WorkerLogger(LoggerFactory.getLogger(getClass))

end CExternalTaskHandler
