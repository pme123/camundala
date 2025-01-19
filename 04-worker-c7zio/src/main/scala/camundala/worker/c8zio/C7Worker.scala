package camundala.worker.c8zio

import camundala.bpmn.GeneralVariables
import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.task
import org.camunda.bpm.client.task as camunda
import zio.*
import zio.ZIO.*
import zio.*

import java.util.Date
import zio.ZIO.*

import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Date
import scala.concurrent.Future

trait C7Worker[In: InOutDecoder, Out: InOutEncoder] extends JobWorker, camunda.ExternalTaskHandler:

  protected def c7Context: C7Context

  private lazy val runtime = Runtime.default

  def client: C7Client = OAuth2Client

  def logger: WorkerLogger = Slf4JLogger.logger(getClass.getName)

  override def execute(
      externalTask: camunda.ExternalTask,
      externalTaskService: camunda.ExternalTaskService
  ): Unit =
    Future:
      val startDate = new Date()
      logger.info(
        s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) started > ${externalTask.getBusinessKey}"
      )
      // executeWorker(externalTaskService)(using externalTask)
      logger.info(
        s"Worker: ${externalTask.getTopicName} (${externalTask.getId}) ended ${printTimeOnConsole(startDate)}   > ${externalTask.getBusinessKey}"
      )

  end execute

end C7Worker
