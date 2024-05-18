package camundala.helper.openApi

import io.swagger.v3.oas.models.media.Schema

import scala.jdk.CollectionConverters.*

case class OpenApiGenerator()(using config: OpenApiConfig, apiDefinition: ApiDefinition):

  lazy val generate: Unit =
    BpmnGenerator().generate
    WorkerGenerator().generate
  end generate
end OpenApiGenerator
