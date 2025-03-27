package camundala.helper.dev.update

import camundala.api.DependencyConfig

case class WorkerGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createOrUpdate(workerPath() / "WorkerApp.scala", workerApp)
    createOrUpdate(workerTestPath() / "WorkerTestApp.scala", workerTestApp)
    createOrUpdate(workerConfigPath / "application.yaml", applicationYaml)
    createOrUpdate(workerConfigPath / "banner.txt", banner)
  end generate

  def createProcessWorker(setupElement: SetupElement): Unit =
    createWorker(setupElement, processWorker, processWorkerTest)
  
  def createEventWorker(setupElement: SetupElement): Unit =
    createWorker(setupElement, eventWorker, eventWorkerTest)

  def createWorker(setupElement: SetupElement,
                   worker: SetupElement => String = processElement,
                   workerTest: SetupElement => String = processElementTest): Unit =
    createIfNotExists(
      workerPath(Some(setupElement)),
      worker(setupElement)
    )
    createIfNotExists(
      workerTestPath(Some(setupElement)),
      workerTest(setupElement)
    )
  end createWorker

  private lazy val companyName = config.companyName
  private lazy val workerApp =
    createWorkerApp("WorkerApp")

  private lazy val workerTestApp =
    createWorkerApp("WorkerTestApp", Some(config.apiProjectConfig.dependencies))

  private def createWorkerApp(
      objName: String,
      dependencies: Option[Seq[DependencyConfig]] = None
  ) =
    s"""$helperDoNotAdjustText
       |package ${config.projectPackage}.worker
       |
       |// sbt worker/${dependencies.map(_ => "test:").getOrElse("")}run
       |@SpringBootApplication
       |@Component("${config.projectClassName}$objName")
       |@ConfigurationPropertiesScan
       |@ComponentScan(basePackages = Array(
       |  "camundala.camunda7.worker.oauth",
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
       |))
       |class $objName
       |
       |object $objName:
       |  
       |  def main(args: Array[String]): Unit =
       |    runSpringApp(classOf[$objName], args*)
       |end $objName""".stripMargin

  private def processWorker(setupElement: SetupElement) =
    val SetupElement(_, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.domain.$processName${version.versionPackage}.$workerName.*
       |
       |@SpringConfiguration
       |class ${workerName}Worker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:
       |
       |  lazy val inOutExample = example
       |
       |  def customInit(in: In): InitIn =
       |    InitIn() //TODO add variable initialisation (to simplify the process expressions) or remove function
       |    // NoInput() // if no initialization is needed
       |  
       |end ${workerName}Worker""".stripMargin
    
  private def eventWorker(setupElement: SetupElement) =
    val SetupElement(_, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.domain.$processName${version.versionPackage}.$workerName.*
       |
       |@SpringConfiguration
       |class ${workerName}Worker extends CompanyValidationWorkerDsl[In]:
       |
       |  lazy val inOutExample = example
       |  
       |  // remove it if not needed
       |  override def validate(in: In): Either[CamundalaWorkerError.ValidatorError, In] = super.validate(in)
       |
       |end ${workerName}Worker""".stripMargin

  private def processElement(
      setupElement: SetupElement
  ) =
    val SetupElement(label, processName, workerName, version) = setupElement
    s"""package ${config.projectPackage}
       |package worker.$processName${version.versionPackage}
       |
       |import ${config.projectPackage}.domain.$processName${version.versionPackage}.$workerName.*
       |
       |@SpringConfiguration
       |class ${workerName}Worker extends Company${label.replace("Task", "")}WorkerDsl[In, Out${
        if label == "CustomTask" then "" else ", ServiceIn, ServiceOut"
      }]:
       |
       |${
        if label == "CustomTask"
        then
          """  lazy val customTask = example
            |
            |  override def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
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
         |  test("customInit"):
         |    val in = In()
         |    val out = InitIn()
         |    assertEquals(
         |      worker.customInit(in),
         |      out
         |    )""".stripMargin
      
  private def eventWorkerTest(setupElement: SetupElement) =
    workerTest(setupElement):
      s"""
         |  test("validate"):
         |    val in = In()
         |    assertEquals(
         |      worker.validate(in), 
         |      Right(in)
         |    )
         |""".stripMargin

  private def processElementTest(setupElement: SetupElement) =
    workerTest(setupElement):
      if setupElement.label == "CustomTask"
      then
        s"""
           |  test("runWork"):
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
       |import ${config.projectPackage}.domain.$processName${version.versionPackage}.$workerName.*
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
       |spring.profiles.include: company-defaults # adds your specific company spring boot configuration (camundala-company-worker -> application-company-defaults.yaml)
       |
       |logging:
       |  level:
       |    root: warn
       |    "camundala": info
       |    "${config.companyName}": info
       |    "org.camunda.bpm.client": info
       |    
       |# add here your specific configuration -> REMOVE # DO NOT ADJU...
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
