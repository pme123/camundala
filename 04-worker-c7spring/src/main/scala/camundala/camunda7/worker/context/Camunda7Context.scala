package camundala.camunda7.worker.context

import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ServiceError
import org.camunda.bpm.client.variable.ClientValues
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.context.annotation.Configuration
import zio.ZIO

import scala.reflect.ClassTag

trait Camunda7Context extends EngineContext:

  def getLogger(clazz: Class[?]): WorkerLogger =
    Slf4JLogger.logger(clazz.getName)

  lazy val toEngineObject: Json => Any =
    json => ClientValues.jsonValue(json.toString)

  def sendRequest[ServiceIn: InOutEncoder, ServiceOut: InOutDecoder: ClassTag](
      request: RunnableRequest[ServiceIn]
  ): SendRequestType[ServiceOut] =
    DefaultRestApiClient.sendRequest(request)

end Camunda7Context

@Configuration
class DefaultCamunda7Context extends Camunda7Context


