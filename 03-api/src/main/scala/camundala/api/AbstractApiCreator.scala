package camundala
package api

import camundala.domain.*
import sttp.apispec.openapi.*

trait AbstractApiCreator extends ProcessReferenceCreator:

  protected def apiConfig: ApiConfig = ApiConfig()

  protected given tenantId: Option[String] = apiConfig.tenantId

  protected def basePath: os.Path = apiConfig.basePath

  protected def title: String

  protected def version: String

  protected def servers = List(
    Server(apiConfig.endpoint).description("Local Developer Server")
  )

  protected def info(title: String, description: Option[String]) =
    Info(title, version, description, contact = apiConfig.contact)

  protected def createLink(
      name: String,
      groupAnchor: Option[String] = None
  ): String =
    val projName = apiConfig.docProjectUrl(projectName)
    val anchor = groupAnchor
      .map(a =>
        s"tag/${a.replace(" ", "-")}/operation/${name.replace(" ", "%20")}"
      )
      .getOrElse(s"tag/${name.replace(" ", "-")}")
    s"[$name]($projName/OpenApi.html#$anchor)"
  end createLink

  extension (inOutApi: InOutApi[?, ?])
    def endpointType: String = inOutApi.inOut.getClass.getSimpleName
    def endpointName: String = (inOutApi, inOutApi.inOut.in) match
      case (serviceApi: ServiceWorkerApi[?, ?, ?, ?], _) => serviceApi.name
      case (_, gs: GenericServiceIn) => gs.serviceName
      case _ => s"$endpointType: ${inOutApi.id}"

end AbstractApiCreator
