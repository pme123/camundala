package camundala
package camunda7.worker

import worker.*
import camundala.worker.CamundalaWorkerError.UnexpectedError
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

import java.time.LocalDateTime

/**
 * To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
 * parameters.
 */
trait CExternalTaskHandler[T <: Worker[?]] extends ExternalTaskHandler:
  def topic: String
  def worker: T

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
    try {
      (for {
        validatedInput <- worker.inValidator.map(InputValidator(_).validate()).getOrElse(Right(worker.in))

      } yield () //
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

end CExternalTaskHandler
