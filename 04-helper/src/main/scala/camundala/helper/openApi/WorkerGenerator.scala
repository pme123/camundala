package camundala.helper.openApi

import scala.jdk.CollectionConverters.*

case class WorkerGenerator()(using config: OpenApiConfig, apiDefinition: ApiDefinition):

  lazy val generate: Unit =
    os.remove.all(config.workerPath)
    os.makeDir.all(config.workerPath)
    generateExports
    generateWorkers
  end generate

  private lazy val generateExports =
    val content =
      s"""package ${config.workerPackage}
        |
        |def serviceBasePath: String =
        |  s"TODO: my Base Path, like https://mycomp.com/myservice"
        |""".stripMargin
    os.write.over(config.workerPath / s"exports.scala", content)
  end generateExports
  private lazy val generateWorkers =
    apiDefinition.bpmnClasses
      .map:
        generateWorker
      .map:
        case name -> content =>
          os.write.over(config.workerPath / s"${name}Worker.scala", content)

  private def generateWorker(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.name

    name ->
      s"""package ${config.workerPackage}
         |
         |import camundala.worker.CamundalaWorkerError.*
         |import org.springframework.context.annotation.Configuration
         |
         |import ${config.bpmnPackage}.*
         |import ${config.bpmnPackage}.$name.*
         |
         |@Configuration
         |class ${name}Worker
         |  extends ${config.superWorkerClass}[In, Out, ServiceIn, ServiceOut]:
         |
         |  lazy val serviceTask = example
         |
         |  def apiUri(in: In) =
         |    uri"$$serviceBasePath${bpmnServiceObject.path}"
         |
         |  override def querySegments(in: In) =
         |    ??? //TODO queryKeys("someParams")
         |
         |${
          if bpmnServiceObject.in.nonEmpty then
            """  override protected def inputMapper(
              |      in: In
              |  ) =
              |    ???
              |  end inputMapper
              |""".stripMargin
          else ""
        }
         |${
          if bpmnServiceObject.out.nonEmpty then
            """  override protected def outputMapper(
              |      out: ServiceResponse[ServiceOut],
              |      in: In
              |  ) =
              |    ???
              |  end outputMapper
              |""".stripMargin
          else ""
        }
         |end ${name}Worker
         |
         |""".stripMargin
  end generateWorker

end WorkerGenerator
