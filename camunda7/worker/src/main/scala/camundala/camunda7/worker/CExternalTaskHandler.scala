package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.camunda.bpm.client.task.*

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*

/**
 * To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
 * parameters.
 */
trait CExternalTaskHandler[T <: Worker[?,?]] extends ExternalTaskHandler, CamundaHelper:
  def topic: String
  def worker: T
  def variableNames: Seq[String] = worker.inValidator.variableNames

  protected def defaultHandledErrorCodes: Seq[ErrorCodes] =
    Seq(ErrorCodes.`output-mocked`, ErrorCodes.`validation-failed`)

  override def execute(
                        externalTask: ExternalTask,
                        externalTaskService: ExternalTaskService
                      ): Unit =
    println(
      s"WORKER ${LocalDateTime.now()} ${externalTask.getTopicName} (${externalTask.getId}) started > ${externalTask.getBusinessKey}"
    )
    executeWorker(externalTaskService)(using externalTask)
    println(
      s"WORKER ${LocalDateTime.now()} ${externalTask.getTopicName} (${externalTask.getId}) ended   > ${externalTask.getBusinessKey}"
    )
  end execute

  private def executeWorker(
                             externalTaskService: ExternalTaskService
                           ): HelperContext[Unit] =
    println(s"Worker ${summon[ExternalTask].getTopicName} running")
    val processVariables = ProcessVariablesExtractor.extract(variableNames)
    try {
      (for {
        initializedInput <- worker.executeWorker(processVariables)
        _ = println(s"EXECUTE WORKER: $initializedInput")
      } yield (externalTaskService.handleSuccess(Map.empty)) //
        ).left.map { ex =>
        ()// externalTaskService.handleError(ex)
      }
    } catch { // safety net
      case ex: Throwable =>
        ex.printStackTrace()
       /* externalTaskService.handleError(
          UnexpectedError(errorMsg =
            s"We caught an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
          )
        )*/
    }
  end executeWorker

  extension (externalTaskService: ExternalTaskService)

    private def handleSuccess(
                               filteredOutput: Map[String, Any]
                             ): HelperContext[Unit] =
      externalTaskService.complete(
        summon[ExternalTask],
        filteredOutput.asJava,
        Map.empty.asJava
      )

    private def handleError(
                             error: CamundalaWorkerError
                           ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      (for {
        handledErrors <- extractSeqFromArrayOrString(
          InputParams.handledErrors,
          defaultHandledErrorCodes
        )
        regexHandledErrors <- extractSeqFromArrayOrString(
          InputParams.regexHandledErrors
        )
        errorHandled = error.isMock || handledErrors.contains(
          error.errorCode.toString
        )
        errorRegexHandled = errorHandled && regexHandledErrors.forall(regex =>
          error.errorMsg.matches(s".*$regex.*")
        )
      } yield (errorHandled, errorRegexHandled))
        .flatMap {
          case (true, true) =>
            val mockedOutput = error match
              case error: ErrorWithOutput =>
                error.mockedOutput
              case _ => Map.empty
            filteredOutput(mockedOutput)
              .map { filtered =>
                println(s"Handled Error: ${error.errorCode}: ${error.errorMsg}")
                externalTaskService.handleBpmnError(
                  summon[ExternalTask],
                  s"${error.errorCode}",
                  error.errorMsg,
                  filtered.asJava
                )
              }
          case (true, false) =>
            Left(HandledRegexNotMatchedError(error))
          case _ =>
            Left(error)
        }
        .left
        .map { err =>
          val errMessage = s"${err.errorCode}: ${err.errorMsg}"
          println(s"Unhandled Error: $errMessage")
          externalTaskService.handleFailure(
            summon[ExternalTask],
            errMessage,
            s" $errMessage\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            0,
            0
          ) //TODO implement retry mechanism
        }
    end handleError

  end extension


  private def filteredOutput(
                              allOutputs: Map[String, Any]
                            ): HelperContext[Either[BadVariableError, Map[String, Any]]] =
    extractSeqFromArrayOrString(InputParams.outputVariables)
      .map {
        case filter if filter.isEmpty => allOutputs
        case filter =>
          allOutputs
            .filter { case k -> _ => filter.contains(k) }
      }
  end filteredOutput

end CExternalTaskHandler
