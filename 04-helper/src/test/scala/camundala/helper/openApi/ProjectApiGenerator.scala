package camundala.helper.openApi

// helper/test:run
object ProjectApiGenerator extends App:

  OpenApiGenerator().generate

  private given OpenApiConfig = gravitonConf
  private lazy val typeMappers =
    OpenApiConfig.generalTypeMapping ++
      Seq(
        TypeMapper("AnyValue", "Json", OpenApiConfig.jsonObj)
      )
  private given ApiDefinition = OpenApiCreator().create
  private lazy val camundaConf = OpenApiConfig(
    projectName = "mycompany-services",
    subProjectName = Some("camunda"),
    openApiFile = os.rel / "camundaOpenApi.json",
    typeMappers = typeMappers,
    superWorkerClass = "camundala.worker.ServiceWorkerDsl",
    filterNames = Seq("{id}")
  )

  private lazy val gravitonConf = OpenApiConfig(
    projectName = "valiant-graviton",
    openApiFile = os.rel / "gravitonOpenApi.json",
  )
end ProjectApiGenerator
