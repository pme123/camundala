package camundala
package camunda7.worker

import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.variable.ClientValues
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.context.annotation.Configuration
import scala.reflect.ClassTag

trait Camunda7Context extends EngineContext:

  def getLogger(clazz: Class[?]): WorkerLogger =
    Camunda7WorkerLogger(LoggerFactory.getLogger(clazz))

  lazy val toEngineObject: Json => Any =
    json => ClientValues.jsonValue(json.toString)

  def sendRequest[ServiceIn: InOutEncoder, ServiceOut: InOutDecoder: ClassTag](
      request: RunnableRequest[ServiceIn]
  ): SendRequestType[ServiceOut] =
    DefaultRestApiClient.sendRequest(request)

end Camunda7Context

@Configuration
class DefaultCamunda7Context extends Camunda7Context

case class Camunda7WorkerLogger(private val delegateLogger: Logger) extends WorkerLogger:

  def debug(message: String): Unit =
    if delegateLogger.isDebugEnabled then
      delegateLogger.debug(message)

  def info(message: String): Unit =
    if delegateLogger.isInfoEnabled then
      delegateLogger.info(message)

  def warn(message: String): Unit =
    if delegateLogger.isWarnEnabled then
      delegateLogger.warn(message)

  def error(err: CamundalaWorkerError): Unit =
    if delegateLogger.isErrorEnabled then
      delegateLogger.error(s"Error ${err.causeMsg}")

end Camunda7WorkerLogger
