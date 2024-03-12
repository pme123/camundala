package camundala.helper.openApi

// helper/test:run
object ProjectApiGenerator extends App:

  OpenApiGenerator().generate

  private given OpenApiConfig = OpenApiConfig(
    projectName = "mycompany-services",
    subProjectName = Some("camunda"),
    openApiFile = os.rel / "camundaOpenApi.json",
    typeMappers = typeMappers,
    superWorkerClass = "camundala.worker.ServiceWorkerDsl",
  )
  private lazy val typeMappers =
    OpenApiConfig.generalTypeMapping ++
      Seq(
        TypeMapper("AnyValue", "Json", OpenApiConfig.jsonObj)
      )
  private given ApiDefinition = OpenApiCreator().create

end ProjectApiGenerator
