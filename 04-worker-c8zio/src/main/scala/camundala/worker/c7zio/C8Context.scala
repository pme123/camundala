package camundala.worker.c7zio

import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ServiceError
import zio.ZIO

import scala.reflect.ClassTag

trait C8Context extends EngineContext:

  def getLogger(clazz: Class[?]): WorkerLogger =
    Slf4JLogger.logger(clazz.getName)

  lazy val toEngineObject: Json => Any =
    json => json

  def sendRequest[ServiceIn: InOutEncoder, ServiceOut: InOutDecoder: ClassTag](
      request: RunnableRequest[ServiceIn]
  ): SendRequestType[ServiceOut] = ???
    //DefaultRestApiClient.sendRequest(request)


