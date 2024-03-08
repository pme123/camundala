package camundala.helper.openApi

import scala.jdk.CollectionConverters.*

case class WorkerGenerator()(using val config: OpenApiConfig, val apiDefinition: ApiDefinition)
  extends GeneratorHelper:

  lazy val generate: Unit =
    os.remove.all(workerPath)
    os.makeDir.all(workerPath)
    generateExports
    generateWorkers
  end generate

  protected lazy val workerPath: os.Path = config.workerPath(superClass.versionPackage)
  protected lazy val workerPackage: String = config.workerPackage(superClass.versionPackage)

  private lazy val generateExports =
    val content =
      s"""package $workerPackage
        |
        |def serviceBasePath: String =
        |  s"TODO: my Base Path, like https://mycomp.com/myservice"
        |""".stripMargin
    os.write.over(workerPath / s"exports.scala", content)
  end generateExports
  private lazy val generateWorkers =
    apiDefinition.bpmnClasses
      .map:
        generateWorker
      .map:
        case name -> content =>
          os.write.over(workerPath / s"${name}Worker.scala", content)

  private def generateWorker(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.name
    val superClass = apiDefinition.superClass

    name ->
      s"""package $workerPackage
         |
         |import CamundalaWorkerError.*
         |import org.springframework.context.annotation.Configuration
         |
         |import $bpmnPackage.*
         |import $bpmnPackage.$name.*
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
