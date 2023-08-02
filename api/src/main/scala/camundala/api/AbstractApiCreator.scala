package camundala
package api

import camundala.bpmn.*

import sttp.apispec.openapi.*


trait AbstractApiCreator extends ProcessReferenceCreator:

  protected def apiConfig: ApiConfig = ApiConfig()

  protected implicit def tenantId: Option[String] = apiConfig.tenantId

  protected def basePath: os.Path = apiConfig.basePath

  protected def title: String

  protected def version: String

  protected def servers = List(
    Server(apiConfig.endpoint).description("Local Developer Server")
  )

  protected def info(title: String, description: Option[String]) =
    Info(title, version, description, contact = apiConfig.contact)

  extension (inOutApi: InOutApi[?, ?])
    def endpointType: String = inOutApi.inOut.getClass.getSimpleName
    def endpointName: String = inOutApi.inOut.in match
      case gs: GenericServiceIn => gs.serviceName
      case _ => s"$endpointType: ${inOutApi.id}"

end AbstractApiCreator
