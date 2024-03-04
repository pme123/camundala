package camundala.helper.openApi

import io.swagger.parser.OpenAPIParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.core.models.ParseOptions

import scala.jdk.CollectionConverters.*

case class OpenApiCreator()(using config: OpenApiConfig):

  lazy val create: ApiDefinition =
    bpmnCreator.create(openAPI)
  end create

  private lazy val bpmnCreator = BpmnCreator()

  private lazy val openAPI: OpenAPI = new OpenAPIParser().readLocation(
    config.openApiFile.toString,
    null,
    new ParseOptions()
  ).getOpenAPI

end OpenApiCreator
