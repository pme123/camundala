package camundala
package camunda7.worker

import camundala.worker.CamundalaWorkerError.ServiceError
import worker.*
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl


final case class Camunda7Context(generalVariables: GeneralVariables = GeneralVariables()) 
  extends EngineContext :
  
  protected lazy val toEngineObject: Json => Any =
    json => new JsonValueImpl(json.toString)

  def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder](
                                                            request: RunnableRequest[ServiceIn]
                                                          ): Either[ServiceError, RequestOutput[ServiceOut]] =
    DefaultRestApiClient.sendRequest(request)

end Camunda7Context

