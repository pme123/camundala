package camundala.helper.setup

import camundala.api.docs.DependencyConf

case class WorkerGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(workerAppPath, workerApp)
    createOrUpdate(workerTestAppPath, workerTestApp)

  private lazy val companyName = config.companyName
  private lazy val workerPath =
    config.projectDir / ModuleConfig.workerModule.packagePath(config.projectPath)
  private lazy val workerAppPath = workerPath / "WorkerApp.scala"
  private lazy val workerApp =
    objectContent("WorkerApp")

  private lazy val workerTestPath =
    config.projectDir / ModuleConfig.workerModule.packagePath(
      config.projectPath,
      mainOrTest = "test"
    )
  private lazy val workerTestAppPath = workerTestPath / "WorkerTestApp.scala"
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

end WorkerGenerator
