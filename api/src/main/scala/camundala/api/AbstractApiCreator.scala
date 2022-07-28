package camundala
package api

import camundala.bpmn.*
import io.circe.*
import io.circe.syntax.*
import os.*
import sttp.tapir.EndpointIO.Example
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.json.circe.*
import sttp.tapir.openapi.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.*

import java.text.SimpleDateFormat
import java.util.Date
import scala.reflect.ClassTag
import scala.util.matching.Regex

trait AbstractApiCreator extends ProcessReferenceCreator:

  protected def apiConfig: ApiConfig = ApiConfig()

  protected implicit def tenantId: Option[String] = apiConfig.tenantId

  protected def basePath: Path = apiConfig.basePath

  protected def title: String

  protected def version: String

  protected def servers = List(Server(apiConfig.endpoint).description("Local Developer Server"))

  protected def info(title: String, description: Option[String]) =
    Info(title, version, description, contact = apiConfig.contact)

  extension (inOutApi: InOutApi[?, ?])
    def endpointType: String = inOutApi.inOut.getClass.getSimpleName
    def endpointName: String = inOutApi.inOut.in match
      case gs: GenericServiceIn => gs.serviceName
      case _ => s"$endpointType: ${inOutApi.name}"

end AbstractApiCreator
