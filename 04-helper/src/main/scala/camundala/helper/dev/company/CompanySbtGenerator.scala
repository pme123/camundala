package camundala.helper.dev.company

import camundala.BuildInfo
import camundala.helper.dev.update.*

case class CompanySbtGenerator()(using
    config: DevConfig
):
  lazy val companyName = config.companyName

  lazy val sbtGenerator   = SbtGenerator()
  lazy val generate: Unit =
    createIfNotExists(buildSbtDir, buildSbt)
    sbtGenerator.generateBuildProperties(helperCompanyDoNotAdjustText)
    createOrUpdate(config.sbtProjectDir / "plugins.sbt", pluginsSbt)
    createOrUpdate(config.sbtProjectDir / "ProjectDef.scala", projectDev)
    createOrUpdate(config.sbtProjectDir / "Settings.scala", settings)
  end generate

  private lazy val projectConf = config.apiProjectConf
  private lazy val buildSbtDir = config.projectDir / "build.sbt"

  private lazy val projectDev =
    s"""// $helperCompanyDoNotAdjustText
       |
       |object ProjectDef {
       |  val org = "$companyName"
       |  val name = "$companyName-camundala"
       |  val version = "0.1.0-SNAPSHOT"
       |}
       |""".stripMargin

  private lazy val settings =
    s"""$helperCompanyDoNotAdjustText
       |
       |import com.typesafe.config.ConfigFactory
       |import laika.ast.Path.Root
       |import laika.config.{LinkValidation, SyntaxHighlighting, Version, Versions}
       |import laika.format.Markdown.GitHubFlavor
       |import laika.helium.Helium
       |import laika.helium.config.{Favicon, HeliumIcon, IconLink}
       |import laika.sbt.LaikaPlugin.autoImport.*
       |import sbt.*
       |import sbt.Keys.*
       |import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoKeys, buildInfoPackage}
       |
       |import scala.jdk.CollectionConverters.asScalaBufferConverter
       |
       |object Settings {
       |
       |  val scalaV = "${BuildInfo.scalaVersion}"
       |  val camundalaV = "${BuildInfo.version}"
       |  val camundaV = "${BuildInfo.camundaVersion}"
       |  val mUnitVersion = "${BuildInfo.mUnitVersion}"
       |  // project
       |  val projectOrg = ProjectDef.org
       |  val projectV = ProjectDef.version
       |  val projectName = ProjectDef.name
       |
       |  def buildInfoSettings(additionalKeys: BuildInfoKey*) = Seq(
       |    buildInfoKeys := Seq[BuildInfoKey](
       |      BuildInfoKey("name", s"$$projectOrg-camundala"),
       |      version,
       |      scalaVersion,
       |      sbtVersion,
       |      BuildInfoKey("camundalaV", camundalaV),
       |    ) ++ additionalKeys,
       |    buildInfoPackage := s"$$projectOrg.camundala"
       |  )
       |
       |  def generalSettings(module: Option[String] = None) = Seq(
       |    scalaVersion := scalaV,
       |    autoImportSetting(module),
       |    scalacOptions ++= Seq(
       |      "-Xmax-inlines:200" // is declared as erased, but is in fact used
       |      // "-Vprofile",
       |    )
       |  ) ++ module.map(m => name := s"$$projectName-$$m").toSeq
       |
       |  def autoImportSetting(module: Option[String] = None) =
       |    scalacOptions +=
       |      (module.toSeq.map(m => s"camundala.$$m") ++
       |        Seq(
       |          "java.lang", "java.time", "scala", "scala.Predef", "camundala.domain", "camundala.bpmn",
       |          "io.circe",
       |          "io.circe.generic.semiauto", "io.circe.derivation", "io.circe.syntax", "sttp.tapir",
       |          "sttp.tapir.json.circe"
       |        )).mkString(start = "-Yimports:", sep = ",", end = "")
       |
       |  // docs
       |  lazy val laikaSettings = Seq(
       |    sourcesInBase := false,
       |    laikaConfig := LaikaConfig.defaults
       |      .withConfigValue(LinkValidation.Local)
       |      .withConfigValue("camundala.docs", "https://pme123.github.io/camundala/")
       |      .withRawContent,
       |    Laika / sourceDirectories := Seq(baseDirectory.value / "src" / "docs")
       |    //  .failOnMessages(MessageFilter.None)
       |    //  .renderMessages(MessageFilter.None)
       |    ,
       |    laikaExtensions := Seq(GitHubFlavor, SyntaxHighlighting),
       |    laikaTheme := Helium.defaults.site
       |      .topNavigationBar(
       |        homeLink = IconLink.internal(Root / "index.md", HeliumIcon.home)
       |      )
       |      .site
       |      .favIcons(
       |        Favicon.internal(Root / "favicon.ico", sizes = "32x32")
       |      )
       |      .site
       |      .versions(versions)
       |      .build
       |  )
       |
       |  lazy val config = ConfigFactory.parseFile(new File("00-docs/CONFIG.conf"))
       |  lazy val currentVersion = config.getString("release.tag")
       |  lazy val released = config.getBoolean("released")
       |  lazy val olderVersions = config.getList("releases.older").asScala
       |  lazy val versions = Versions
       |    .forCurrentVersion(Version(currentVersion, currentVersion).withLabel(if (released)
       |      "Stable"
       |    else "Dev"))
       |    .withOlderVersions(
       |      olderVersions.map(_.unwrapped().toString).map(v => Version(v, v)) *
       |    )
       |
       |  def loadingMessage = s\"\"\"Successfully started.
       |                          |- Project: $$projectOrg : $$projectName : $$projectV
       |                          |- Camundala: $$camundalaV
       |                          |- Scala: $$scalaV
       |                          |- Camunda: $$camundaV
       |                          |\"\"\".stripMargin
       |
       |  // dependencies
       |  val typesafeConfigDep = "com.typesafe" % "config" % "1.4.3"
       |
       |  lazy val bpmnDeps = Seq(
       |    "io.github.pme123" %% "camundala-bpmn" % camundalaV
       |  )
       |  lazy val apiDeps = Seq(
       |    "io.github.pme123" %% "camundala-api" % camundalaV,
       |    typesafeConfigDep
       |  )
       |  lazy val dmnDeps = Seq(
       |    "io.github.pme123" %% "camundala-dmn" % camundalaV
       |  )
       |  lazy val simulationDeps = Seq(
       |    "io.github.pme123" %% "camundala-simulation" % camundalaV
       |  )
       |  lazy val workersDeps = Seq(
       |    "io.github.pme123" %% "camundala-camunda7-worker" % camundalaV
       |  )
       |
       |  lazy val helperDeps = apiDeps ++ Seq(
       |    "io.github.pme123" %% "camundala-helper" % camundalaV
       |  )
       |
       |  lazy val unitTestSettings = Seq(
       |    libraryDependencies += "org.scalameta" %% "munit" % mUnitVersion % Test,
       |    testFrameworks += new TestFramework("munit.Framework")
       |  )
       |  // publish
       |
       |  lazy val preventPublication = Seq(
       |    publish := {},
       |    publishArtifact := false,
       |    publishLocal := {}
       |  )
       |}
       |""".stripMargin

  private lazy val buildSbt =
    s"""// $howToResetText
       |import sbt.*
       |import sbt.Keys.*
       |import Settings.*
       |
       |ThisBuild / version := projectV
       |ThisBuild / organization := projectOrg
       |ThisBuild / onLoadMessage := loadingMessage
       |
       |lazy val root = (project in file("."))
       |  .settings(name := projectName, sourcesInBase := false)
       |  .settings(preventPublication)
       |  .aggregate(
       |    bpmn,
       |    api,
       |    dmn,
       |    simulation,
       |    worker,
       |    helper,
       |    docs
       |  )
       |
       |lazy val bpmn = project
       |  .in(file("./02-bpmn"))
       |  .settings(generalSettings(Some("bpmn")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= bpmnDeps)
       |  .settings(buildInfoSettings())
       |  .enablePlugins(BuildInfoPlugin)
       |
       |lazy val api = project
       |  .in(file("./03-api"))
       |  .settings(generalSettings(Some("api")))
       |  .settings(publicationSettings)
       |  .settings(unitTestSettings)
       |  .settings(libraryDependencies ++= apiDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val dmn = project
       |  .in(file("./03-dmn"))
       |  .settings(generalSettings(Some("dmn")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= dmnDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val simulation = project
       |  .in(file("./03-simulation"))
       |  .settings(generalSettings(Some("simulation")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= simulationDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val worker = project
       |  .in(file("./03-worker"))
       |  .settings(generalSettings(Some("worker")))
       |  .settings(publicationSettings)
       |  .settings(unitTestSettings)
       |  .settings(libraryDependencies ++= workersDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val helper = project
       |  .in(file("./04-helper"))
       |  .settings(generalSettings(Some("helper")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= helperDeps)
       |  .dependsOn(api, simulation)
       |
       |lazy val docs = project
       |  .in(file("./00-docs"))
       |  .settings(
       |    name := s"$$projectName-docs"
       |  )
       |  .settings(generalSettings())
       |  .settings(preventPublication)
       |  .dependsOn(helper)
       |  .settings(laikaSettings)
       |  .enablePlugins(LaikaPlugin)
       |
       |// Releasing
       |
       |lazy val publicationSettings = Seq(
       |  //TODO credentials ++= Seq(repoCredentials),
       |  isSnapshot := false,
       |  //TODO publishTo := Some(releaseRepo),
       |  // Enables publishing to maven repo
       |  publishMavenStyle := true,
       |  packageDoc / publishArtifact := false,
       |  // disable using the Scala version in output paths and artifacts
       |  // crossPaths := false,
       |  //TODO resolvers ++= Seq(releaseRepo),
       |  // logLevel := Level.Debug,
       |)
       |
       |//TODO lazy val repoCredentials: Credentials = ???
       |//TODO lazy val releaseRepo = ???
       |""".stripMargin
  end buildSbt

  private lazy val pluginsSbt =
    s"""$helperCompanyDoNotAdjustText
       |addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
       |
       |addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
       |addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
       |
       |// docs
       |addSbtPlugin("org.typelevel" % "laika-sbt" % "1.3.1")
       |
       |// docker (optional)
       |addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
       |""".stripMargin

end CompanySbtGenerator
