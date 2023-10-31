package camundala
package camunda7.worker

import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ServiceError
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl
import org.springframework.context.annotation.Configuration

@Configuration
class Camunda7Context extends EngineContext:

  lazy val toEngineObject: Json => Any =
    json => new JsonValueImpl(json.toString)

  def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder](
      request: RunnableRequest[ServiceIn]
  ): Either[ServiceError, RequestOutput[ServiceOut]] =
    DefaultRestApiClient.sendRequest(request)

end Camunda7Context

