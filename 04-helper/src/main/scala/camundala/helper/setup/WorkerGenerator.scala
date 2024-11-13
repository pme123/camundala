package camundala.helper.setup

import camundala.api.docs.DependencyConf

case class WorkerGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createIfNotExists(workerPath() / "WorkerApp.scala", workerApp)
    createIfNotExists(workerTestPath() / "WorkerTestApp.scala", workerTestApp)
    createOrUpdate(workerConfigPath / "application.yaml", applicationYaml)
    createOrUpdate(workerConfigPath / "banner.txt", banner)
  end generate

  def createProcess(setupElement: SetupElement): Unit =
    os.write.over(
      workerPath(Some(setupElement)),
      processWorker(setupElement)
    )
    os.write.over(
      workerTestPath(Some(setupElement)),
      processWorkerTest(setupElement)
    )
  end createProcess

  def createProcessElement(setupElement: SetupElement): Unit =
    os.write.over(
      workerPath(Some(setupElement)),
      processElement(setupElement)
    )
    os.write.over(
      workerTestPath(Some(setupElement)),
      processElementTest(setupElement)
    )
  end createProcessElement

  private lazy val companyName = config.companyName
  private lazy val workerApp =
    objectContent("WorkerApp")

  private lazy val workerTestApp =
    objectContent("WorkerTestApp", Some(config.apiProjectConf.dependencies))

  private def objectContent(
      objName: String,
      dependencies: Option[Seq[DependencyConf]] = None
  ) =
    s"""package ${config.projectPackage}.worker
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

  private def processWorker(setupElement: SetupElement) =
    val SetupElement(_, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.bpmn.$processName${version.versionPackage}.$workerName.*
       |
       |@Configuration
       |class ${workerName}Worker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:
       |
       |  lazy val inOutExample = example
       |
       |  override def customInit(in: In): InitIn =
       |    InitIn() //TODO add variable initialisation (to simplify the process expressions) or remove function
       |    // NoInput() // if no initialization is needed
       |  
       |end ${workerName}Worker""".stripMargin
  end processWorker

  private def processElement(
      setupElement: SetupElement
  ) =
    val SetupElement(label, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.bpmn.$processName${version.versionPackage}.$workerName.*
       |
       |@Configuration
       |class ${workerName}Worker extends Company${label.replace("Task", "")}WorkerDsl[In, Out${
        if label == "CustomTask" then "" else ", ServiceIn, ServiceOut"
      }]:
       |
       |${
        if label == "CustomTask"
        then
          """  lazy val customTask = example
            |
            |  def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
            |    ???
            |  end runWork""".stripMargin
        else
          """
            |  lazy val serviceTask = example
            |
            |  override lazy val method = Method.GET
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
            |  ) = ???
            |
            |""".stripMargin
      }
       |
       |
       |end ${workerName}Worker""".stripMargin
  end processElement

  private def processWorkerTest(setupElement: SetupElement) =
    workerTest(setupElement):
      s"""
         |  test("customInit ${setupElement.bpmnName}"):
         |    val in = In()
         |    val out = InitIn()
         |    assertEquals(
         |      worker.customInit(in),
         |      out
         |    )""".stripMargin

  private def processElementTest(setupElement: SetupElement) =
    workerTest(setupElement):
      if setupElement.label == "CustomTask"
      then
        s"""
           |  test("runWork ${setupElement.bpmnName}"):
           |    val in = In()
           |    val out = Right(Out())
           |    assertEquals(
           |      worker.runWork(in),
           |      out
           |    )
           |""".stripMargin
      else
        s"""
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
           |""".stripMargin
  end processElementTest

  private def workerTest(setupElement: SetupElement)(tests: String) =
    val SetupElement(_, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.bpmn.$processName${version.versionPackage}.$workerName.*
       |import ${config.projectPackage}.worker.$processName${version.versionPackage}.${workerName}Worker
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
  end workerTest

  private lazy val applicationYaml =
    s"""# DO NOT ADJUST. This file is replaced by `./helper.scala update`.
       |spring.application.name: ${config.projectName}-worker
       |
       |server:
       |  port: 8093
       |camunda.bpm:
       |  job-execution:
       |    wait-time-in-millis: 200 # this is for speedup testing
       |  client:
       |    base-url: $${CAMUNDA_BASE_URL:http://localhost:8080/engine-rest}
       |    worker-id: $${WORKER_ID:my-worker}
       |    disable-backoff-strategy: true # only during testing - faster topic
       |    async-response-timeout: 10000
       |
       |
       |logging:
       |  level:
       |    root: warn
       |    "camundala": info
       |    "${config.companyName}": info
       |    "org.camunda.bpm.client": info
       |
       |spring.profiles.include: camunda-default # adds your specific company spring boot configuration (application-camunda-default.yaml)
       |
       |""".stripMargin

  private lazy val banner =
    s"""# DO NOT ADJUST. This file is replaced by `./helper.scala update`.
       |
       |     _/_/_/                                                      _/            _/
       |  _/          _/_/_/  _/_/_/  _/_/    _/    _/  _/_/_/      _/_/_/    _/_/_/  _/    _/_/_/
       | _/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
       |_/        _/    _/  _/    _/    _/  _/    _/  _/    _/  _/    _/  _/    _/  _/  _/    _/
       | _/_/_/    _/_/_/  _/    _/    _/    _/_/_/  _/    _/    _/_/_/    _/_/_/  _/    _/_/_/
       |
       | $${spring.application.name}
       |                                                          >>> the Scala DSL for Camunda
       |
       |  Spring-Boot: $${spring-boot.formatted-version}
       |  Runs on port $${server.port}
       |  Connects to $${camunda.bpm.client.base-url}
       |""".stripMargin

  private def workerPath(setupElement: Option[SetupElement] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath
    ) /
      setupElement
        .map: se =>
          os.rel / se.processName / se.version.versionPath
        .getOrElse(os.rel)

    os.makeDir.all(dir)
    dir / setupElement
      .map: se =>
        os.rel / s"${se.bpmnName}Worker.scala"
      .getOrElse(os.rel)
  end workerPath

  private def workerTestPath(setupElement: Option[SetupElement] = None) =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
      mainOrTest = "test"
    ) /
      setupElement
        .map: se =>
          os.rel / se.processName / se.version.versionPath
        .getOrElse(os.rel)
    os.makeDir.all(dir)
    dir / setupElement
      .map: se =>
        os.rel / s"${se.bpmnName}WorkerTest.scala"
      .getOrElse(os.rel)
  end workerTestPath

  private lazy val workerConfigPath =
    val dir = config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
      isSourceDir = false
    )
    os.makeDir.all(dir)
    dir
  end workerConfigPath
end WorkerGenerator
