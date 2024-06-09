package camundala.helper.setup

case class SbtSettingsGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(config.sbtProjectDir / "Settings.scala", settingsSbt)
  end generate

  private lazy val versionConfig = config.versionConfig
  private lazy val repoConfig = config.reposConfig
  private lazy val settingsSbt =
    s"""// $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |
       |import com.typesafe.sbt.SbtNativePackager.Docker
       |import com.typesafe.sbt.packager.Keys.*
       |import sbt.*
       |import sbt.Keys.*
       |
       |object Settings {
       |
       |  val scalaV = "${versionConfig.scalaVersion}"
       |  val customer = ProjectDef.org
       |  val customerCamundalaV = "${versionConfig.customerCamundalaVersion}"
       |
       |  // other dependencies
       |  // run worker
       |  val springBootVersion = "${versionConfig.springBootVersion}"
       |  val jaxbXmlVersion = "${versionConfig.jaxbXmlVersion}"
       |  val mUnitVersion = "${config.versionConfig.munitVersion}"
       |    
       |  // for running worker
       |  val springBoot =  "org.springframework.boot" % "spring-boot-starter-oauth2-client" % springBootVersion % Provided
       |  val jaxbXml =  "javax.xml.bind" % "jaxb-api" % jaxbXmlVersion % Provided
       |  val mUnit = "org.scalameta" %% "munit" % mUnitVersion % Test
       |    
       |$projectSettings
       |$sbtDependencies
       |$sbtPublish
       |$sbtRepos
       |$sbtDocker
       |$testSettings
       |
       |  lazy val loadingMessage = s\"\"\"Successfully started
       |- Dependencies:
       |  - Camundala: ${versionConfig.camundalaVersion}
       |  - Customer-Camundala: $$customerCamundalaV
       |  - Scala: $$scalaV
       |${versionConfig.otherVersions.map { case k -> v => s"  - $k: $v" }.mkString("\n")}
       |- Package Config:
       |  - org: $${ProjectDef.org}
       |  - name: $${ProjectDef.name}
       |  - version: $${ProjectDef.version}
       |  - dependencies: $${ProjectDef.bpmnDependencies.map(_.toString()).sorted.mkString("\\n    - ", "\\n    - ", "")}
       |  \"\"\"
       |$sbtAutoImportSetting
       |}""".stripMargin

  private lazy val projectSettings =
    s"""  def projectSettings(
       |                       module: Option[String] = None,
       |                       postfix: Option[String] = None
       |                     ) = Seq(
       |    name := s"$${ProjectDef.name}$${module.map(p => s"-$$p").getOrElse("")}",
       |    organization := ProjectDef.org,
       |    version := ProjectDef.version,
       |    scalaVersion := scalaV,
       |    scalacOptions ++= Seq(
       |      // "-deprecation", // Emit warning and location for usages of deprecated APIs.
       |      // "-feature", // Emit warning and location for usages of features that should be imported explicitly.
       |      // "-rewrite", "-source", "3.4-migration", // migrate automatically to scala 3.4
       |      "-Xmax-inlines:200" // is declared as erased, but is in fact used
       |      // "-Vprofile",
       |    ),
       |    javaOptions ++= Seq(
       |      "-Xmx3g",
       |      "-Xss2m",
       |      "-XX:+UseG1GC",
       |      "-XX:InitialCodeCacheSize=512m",
       |      "-XX:ReservedCodeCacheSize=512m",
       |      "-Dfile.encoding=UTF8"
       |    ),
       |    credentials ++= Seq(${repoConfig.sbtCredentials}),
       |    resolvers ++= Seq(${repoConfig.sbtRepos}),
       |    autoImportSetting(
       |      (postfix orElse module).toSeq.flatMap(x =>
       |         Seq(s"camundala.$$x", s"$$customer.camundala.$$x")
       |      )
       |    )
       |  )
       |""".stripMargin
  private lazy val sbtPublish =
    s"""  lazy val preventPublication = Seq(
       |    publish / skip := true,
       |    publish := {},
       |    publishArtifact := false,
       |    publishLocal := {}
       |  )
       |
       |  lazy val publicationSettings = Seq(
       |    publishTo := Some(${repoConfig.repos.head.name}Repo),
       |    // Enables publishing to maven repo
       |    publishMavenStyle := true,
       |    packageDoc / publishArtifact := false,
       |    // logLevel := Level.Debug,
       |    // disable using the Scala version in output paths and artifacts
       |    crossPaths := false
       |  )""".stripMargin

  private lazy val sbtDependencies =
    config.modules
      .map: moduleConfig =>
        val name = moduleConfig.name
        val dependencies = moduleConfig.sbtDependencies
        s"""
           |  lazy val ${name}Deps = ${
            if moduleConfig.hasProjectDependencies then s"ProjectDef.${name}Dependencies ++" else ""
          }
           |    Seq(${
            if dependencies.nonEmpty then dependencies.mkString("\n      ", ",\n      ", ",")
            else ""
          }
           |      customer %% s"$$customer-camundala-$name" % customerCamundalaV
           |    )
           |""".stripMargin
      .mkString
  private lazy val sbtRepos =
    s"""// Credentials
       |${
        repoConfig.credentials
          .map:
            _.sbtContent
          .mkString
      }
       |// Repos
       |${
        repoConfig.repos
          .map:
            _.sbtContent
          .mkString
      }""".stripMargin

  private lazy val sbtDocker =
    s"""
       |  lazy val dockerSettings = ${config.sbtDockerSettings}
       |""".stripMargin
  private lazy val testSettings =
    s"""  lazy val testSettings = Seq(
       |    libraryDependencies += mUnit,
       |    Test / parallelExecution := true,
       |    testFrameworks += new TestFramework("munit.Framework")
       |  )
       |""".stripMargin

  private lazy val sbtAutoImportSetting =
    """  def autoImportSetting(customAutoSettings: Seq[String]) =
      |    scalacOptions +=
      |      (customAutoSettings ++
      |        Seq(
      |          "java.lang",
      |          "java.time", 
      |          "scala",
      |          "scala.Predef",
      |          "camundala.domain",
      |          "camundala.bpmn",
      |          s"$customer.camundala.bpmn",
      |          "io.circe.syntax", 
      |          "sttp.tapir.json.circe"
      |        )).mkString(start = "-Yimports:", sep = ",", end = "")
      |""".stripMargin

end SbtSettingsGenerator
