package camundala.helper.openApi

import io.swagger.v3.oas.models.media.Schema

import scala.jdk.CollectionConverters.*

case class BpmnGenerator()(using config: OpenApiConfig, apiDefinition: ApiDefinition):

  lazy val generate: Unit =
    os.remove.all(config.bpmnPath)
    os.makeDir.all(config.bpmnPath)
    BpmnSuperClassGenerator().generate
    ServiceClassesGenerator().generate
    BpmnClassesGenerator().generate
  end generate
end BpmnGenerator




