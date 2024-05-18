package camundala.helper.openApi

import scala.jdk.CollectionConverters.*

case class WorkerGenerator()(using val config: OpenApiConfig, val apiDefinition: ApiDefinition)
    extends GeneratorHelper:

  lazy val generate: Unit =
    os.remove.all(workerPath)
    os.makeDir.all(workerPath)
    generateExports
    generateWorkers
    generateWorkerTests
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

  private lazy val generateWorkerTests =
    apiDefinition.bpmnClasses
      .map:
        generateWorkerTest
      .map:
        case name -> content =>
          os.write.over(workerPath / s"${name}WorkerTest.scala", content)

  private def generateWorker(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.className
    val superClass = apiDefinition.superClass

    name ->
      s"""package $workerPackage
         |
         |import CamundalaWorkerError.*
         |import org.springframework.context.annotation.Configuration
         |
         |import $bpmnPackage.*
         |import $bpmnPackage.schema.*
         |import $bpmnPackage.$name.*
         |
         |@Configuration
         |class ${name}Worker
         |  extends ${config.superWorkerClass}[In, Out, ServiceIn, ServiceOut]:
         |
         |  lazy val serviceTask = example
         |
         |  override lazy val method = Method.${bpmnServiceObject.method}
         |
         |  def apiUri(in: In) =
         |    uri"$$serviceBasePath${bpmnServiceObject.path.replace("{", "${in.")}"
         |
         |  override def validate(in: In): Either[ValidatorError, In] =
         |    ??? // additional validation
         |
         |  override def inputHeaders(in: In) =
         |    ??? // etagInHeader(in.etag)
         |        // requestIdInHeader(in.requestId)
         |
         |  override def querySegments(in: In) =
         |    ??? //TODO queryKeys("someParams")
         |
         |${
          if bpmnServiceObject.in.nonEmpty then
            """  override def inputMapper(
              |      in: In
              |  ) =
              |    ???
              |  end inputMapper
              |""".stripMargin
          else ""
        }
         |${
          if bpmnServiceObject.out.nonEmpty then
            """  override def outputMapper(
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

  private def generateWorkerTest(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.className
    val superClass = apiDefinition.superClass

    name ->
      s"""package $workerPackage
         |
         |import CamundalaWorkerError.*
         |
         |import $bpmnPackage.*
         |import $bpmnPackage.schema.*
         |import $bpmnPackage.$name.*
         |
         |//sbt worker/testOnly *${name}WorkerTest
         |class ${name}WorkerTest
         |  extends munit.FunSuite:
         |
         |  lazy val worker = ${name}Worker()
         |
         |  test("apiUri"):
         |    assertEquals(
         |      worker.apiUri(In()).toString,
         |      s"NOT-SET/YourPath"
         |    )
         |
         |  test("inputMapper"):
         |    assertEquals(
         |      worker.inputMapper(In()),
         |      Some(ServiceIn())
         |    )
         |
         |  test("outputMapper"):
         |    assertEquals(
         |      worker.outputMapper(
         |        ServiceResponse(ServiceOut()),
         |        In()
         |      ),
         |      Right(Out())
         |    )
         |end ${name}WorkerTest
         |""".stripMargin
  end generateWorkerTest
end WorkerGenerator
