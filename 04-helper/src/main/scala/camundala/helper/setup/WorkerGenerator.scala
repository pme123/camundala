package camundala.helper.setup

import camundala.api.docs.DependencyConf

case class WorkerGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(workerPath() / "WorkerApp.scala", workerApp)
    createOrUpdate(workerTestPath() / "WorkerTestApp.scala", workerTestApp)

  def createProcess(processName: String): Unit =
    val workerName = processName.head.toUpper + processName.tail
    os.write.over(
      workerPath (Some(processName)) / s"${workerName}Worker.scala",
      processWorker(processName, workerName)
    )
    os.write.over(
      workerTestPath(Some(processName)) / s"${workerName}WorkerTest.scala",
      processWorkerTest(processName, workerName)
    )
  end createProcess

  def createCustomWorker(processName: String, workerName: String): Unit =
    os.write.over(
      workerPath (Some(processName)) / s"${workerName}Worker.scala",
      customWorker(processName, workerName)
    )
    os.write.over(
      workerTestPath(Some(processName)) / s"${workerName}WorkerTest.scala",
      customWorkerTest(processName, workerName)
    )
  end createCustomWorker

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

  private def processWorkerTest(
      processName: String,
      workerName: String
  ) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |import worker.$processName.${workerName}Worker
       |
       |class ${workerName}WorkerTest extends munit.FunSuite:
       |
       |  lazy val worker = ${workerName}Worker()
       |
       |  test("customInit $workerName"):
       |    val in = In()
       |    val out = Map.empty[String, Any]
       |    assertEquals(
       |      worker.customInit(in),
       |      out
       |    )
       |        
       |  
       |end ${workerName}WorkerTest""".stripMargin

  private def customWorkerTest(
      processName: String,
      workerName: String
  ) =
    s"""package ${config.projectPackage}
       |package worker.$processName
       |
       |import bpmn.$processName.$workerName.*
       |import worker.$processName.${workerName}Worker
       |
       |class ${workerName}WorkerTest extends munit.FunSuite:
       |
       |  lazy val worker = ${workerName}Worker()
       |
       |  test("runWork $workerName"):
       |    val in = In()
       |    val out = Right(Out())
       |    assertEquals(
       |      worker.runWork(in),
       |      out
       |    )
       |
       |
       |end ${workerName}WorkerTest""".stripMargin

  private def workerPath(processName: Option[String] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
    ) / processName.toSeq
    os.makeDir.all(dir)
    dir
  end workerPath

  private def workerTestPath(processName: Option[String] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
      mainOrTest = "test",
    ) / processName.toSeq
    os.makeDir.all(dir)
    dir
  end workerTestPath
end WorkerGenerator
