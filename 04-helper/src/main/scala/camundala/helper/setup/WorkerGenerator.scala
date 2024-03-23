package camundala.helper.setup

import camundala.api.docs.DependencyConf

case class WorkerGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(workerPath() / "WorkerApp.scala", workerApp)
    createOrUpdate(workerTestPath() / "WorkerTestApp.scala", workerTestApp)

  def createProcess(processName: String): Unit =
    val workerName = processName.head.toUpper + processName.tail
    os.write.over(
      workerPath(Some(processName)) / s"${workerName}Worker.scala",
      processWorker(processName, workerName)
    )
    os.write.over(
      workerTestPath(Some(processName)) / s"${workerName}WorkerTest.scala",
      processWorkerTest(processName, workerName)
    )
  end createProcess

  def createCustomWorker(processName: String, workerName: String): Unit =
    os.write.over(
      workerPath(Some(processName)) / s"${workerName}Worker.scala",
      customWorker(processName, workerName)
    )
    os.write.over(
      workerTestPath(Some(processName)) / s"${workerName}WorkerTest.scala",
      customWorkerTest(processName, workerName)
    )
  end createCustomWorker

  def createServiceWorker(processName: String, workerName: String): Unit =
    os.write.over(
      workerPath(Some(processName)) / s"${workerName}Worker.scala",
      serviceWorker(processName, workerName)
    )
    os.write.over(
      workerTestPath(Some(processName)) / s"${workerName}WorkerTest.scala",
      serviceWorkerTest(processName, workerName)
    )
  end createServiceWorker

  private lazy val companyName = config.companyName
  private lazy val workerApp =
    objectContent("WorkerApp")

  private lazy val workerTestApp =
    objectContent("WorkerTestApp", Some(config.apiProjectConf.dependencies))

  private def objectContent(
      objName: String,
      dependencies: Option[Seq[DependencyConf]] = None
  ) =
    s"""package $companyName.camundala.worker
       |
       |import org.springframework.boot.SpringApplication
       |import org.springframework.boot.autoconfigure.SpringBootApplication
       |import org.springframework.boot.context.properties.ConfigurationPropertiesScan
       |import org.springframework.context.annotation.ComponentScan
       |import org.springframework.stereotype.Component
       |
       |// sbt worker/${dependencies.map(_ => "test:").getOrElse("")}run
       |@SpringBootApplication
       |@Component("${config.projectClassName}$objName")
       |@ConfigurationPropertiesScan
       |@ComponentScan(basePackages = Array(
       |  "$companyName.camundala.worker",
       |  "${config.projectPackage}.worker",
       |  ${
        dependencies
          .map:
            _.map(_.projectPackage + ".worker")
              .map(d => s"\"$d\"")
              .mkString(",\n  ")
          .map:
            _ +
              (if dependencies.get.nonEmpty then ",\n" else "  //TODO add here your dependencies")
          .getOrElse("")
      }
       |  //TODO add packages you need for your Spring App
       |))
       |class $objName
       |
       |object $objName:
       |  
       |  def main(args: Array[String]): Unit =
       |    SpringApplication.run(classOf[$objName], args: _*)
       |end $objName""".stripMargin

  private def processWorker(
      processName: String,
      workerName: String
  ) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |
       |@Configuration
       |class ${workerName}Worker extends CompanyInitWorkerDsl[In, Out, InConfig]:
       |
       |  lazy val inOutExample = example
       |
       |  override def customInit(in: In): Map[String, Any] =
       |    Map() //TODO add variable initialisation (to simplify the process expressions) or remove function
       |  
       |end ${workerName}Worker""".stripMargin

  private def customWorker(
      processName: String,
      workerName: String
  ) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |
       |@Configuration
       |class ${workerName}Worker extends CompanyCustomWorkerDsl[In, Out]:
       |
       |  lazy val customTask = example
       |
       |  def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
       |    ???
       |  end runWork
       |
       |end ${workerName}Worker""".stripMargin

  private def serviceWorker(
      processName: String,
      workerName: String
  ) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |import camundala.worker.CamundalaWorkerError.*
       |
       |@Configuration
       |class ${workerName}Worker
       |    extends CompanyServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]:
       |
       |  lazy val serviceTask = example
       |
       |  override lazy val method = ??? // default is Method.GET
       |
       |  def apiUri(in: In) = uri"your/path/TODO"
       |
       |  override def querySegments(in: In) = ???
       |    // queryKeys(ks: String*)
       |    // queryKeyValues(kvs: (String, Any)*)
       |    // queryValues(vs: Any*)
       |
       |  override def inputHeaders(in: In) = ???
       |
       |  override def inputMapper(in: In): Option[ServiceIn] = ???
       |
       |  override def outputMapper(
       |      out: ServiceResponse[ServiceOut],
       |      in: In
       |  ): Either[ServiceMappingError, Out] = ???
       |
       |
       |end ${workerName}Worker""".stripMargin

  private def processWorkerTest(
      processName: String,
      workerName: String
  ) =
    workerTest(
      processName,
      workerName
    ):
      s"""
       |  test("customInit $workerName"):
       |    val in = In()
       |    val out = Map.empty[String, Any]
       |    assertEquals(
       |      worker.customInit(in),
       |      out
       |    )""".stripMargin

  private def customWorkerTest(
      processName: String,
      workerName: String
  ) =
    workerTest(
      processName,
      workerName
    ):
      s"""
         |  test("runWork $workerName"):
         |    val in = In()
         |    val out = Right(Out())
         |    assertEquals(
         |      worker.runWork(in),
         |      out
         |    )
         |""".stripMargin

  private def serviceWorkerTest(
      processName: String,
      workerName: String
  ) =
    workerTest(
      processName,
      workerName
    ):
      s"""
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
         |""".stripMargin

  private def workerTest(
      processName: String,
      workerName: String
  )(tests: String) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |import worker.$processName.${workerName}Worker
       |
       |//sbt worker/testOnly *${workerName}WorkerTest
       |class ${workerName}WorkerTest extends munit.FunSuite:
       |
       |  lazy val worker = ${workerName}Worker()
       |
       |$tests
       |
       |
       |end ${workerName}WorkerTest""".stripMargin

  private def workerPath(processName: Option[String] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath
    ) / processName.toSeq
    os.makeDir.all(dir)
    dir
  end workerPath

  private def workerTestPath(processName: Option[String] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
      mainOrTest = "test"
    ) / processName.toSeq
    os.makeDir.all(dir)
    dir
  end workerTestPath
end WorkerGenerator
