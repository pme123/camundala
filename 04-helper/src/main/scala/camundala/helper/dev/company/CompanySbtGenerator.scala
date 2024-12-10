package camundala.helper.dev.company

import camundala.BuildInfo
import camundala.helper.dev.update.*
import camundala.helper.util.{TestType, VersionHelper}

case class CompanySbtGenerator()(using
                                 config: DevConfig
):
  lazy val sbtGenerator = SbtGenerator()
  lazy val generate: Unit =
    createOrUpdate(buildSbtDir, buildSbt)
    sbtGenerator.generateBuildProperties
    generatePluginsSbt
  end generate

  lazy val generatePluginsSbt =
    createOrUpdate(config.sbtProjectDir / "plugins.sbt", pluginsSbt)
  private lazy val projectConf = config.apiProjectConf
  private lazy val versionHelper = VersionHelper(projectConf)
  private lazy val buildSbtDir = config.projectDir / "build.sbt"

  private lazy val buildSbt =
    s"""// $howToResetText
       |// versions
       |val projectV = "0.1.0-SNAPSHOT"
       |val scalaV = "${BuildInfo.scalaVersion}"
       |val camundalaV = "${VersionHelper.camundalaVersion}"
       |val dmnTesterVersion = "0.17.10"
       |val camundaV = "7.21.0"
       |val mUnitVersion = "1.0.0"
       |val companyName = "${config.companyName}"
       |ThisBuild / version := projectV
       |ThisBuild / organization := companyName
       |ThisBuild / onLoadMessage := loadingMessage
       |
       |lazy val root = (project in file("."))
       |  .settings(name := s"$$companyName-camundala", sourcesInBase := false)
       |  .settings(preventPublication)
       |  .aggregate(
       |    bpmn,
       |    api,
       |    dmn,
       |    simulation,
       |    worker,
       |    helper
       |  )
       |
       |lazy val bpmn = project
       |  .in(file("./02-bpmn"))
       |  .settings(
       |    name := s"$$companyName-camundala-bpmn",
       |    buildInfoKeys := Seq[BuildInfoKey](
       |      name,
       |      version,
       |      scalaVersion,
       |      sbtVersion,
       |      BuildInfoKey("camundalaV", camundalaV)
       |    )
       |  )
       |  .settings(generalSettings())
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= bpmnDeps)
       |  .enablePlugins(BuildInfoPlugin)
       |
       |lazy val api = project
       |  .in(file("./03-api"))
       |  .settings(
       |    name := s"$$companyName-camundala-api"
       |  )
       |  .settings(generalSettings(Some("api")))
       |  .settings(publicationSettings)
       |  .settings(unitTestSettings)
       |  .settings(libraryDependencies ++= apiDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val dmn = project
       |  .in(file("./03-dmn"))
       |  .settings(
       |    name := s"$$companyName-camundala-dmn"
       |  )
       |  .settings(generalSettings(Some("dmn")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= dmnDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val simulation = project
       |  .in(file("./03-simulation"))
       |  .settings(
       |    name := s"$$companyName-camundala-simulation"
       |  )
       |  .settings(generalSettings(Some("simulation")))
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= simulationDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val worker = project
       |  .in(file("./03-worker"))
       |  .settings(
       |    name := s"$$companyName-camundala-worker"
       |  )
       |  .settings(generalSettings(Some("worker")))
       |  .settings(publicationSettings)
       |  .settings(unitTestSettings)
       |  .settings(libraryDependencies ++= workersDeps)
       |  .dependsOn(bpmn)
       |
       |lazy val helper = project
       |  .in(file("./04-helper"))
       |  .settings(
       |    name := s"$$companyName-camundala-helper"
       |  )
       |  .settings(generalSettings())
       |  .settings(publicationSettings)
       |  .settings(libraryDependencies ++= helperDeps)
       |  .dependsOn(api, simulation)
       |
       |// Releasing
       |
       |lazy val publicationSettings = Seq(
       |  isSnapshot := false,
       |  //publishTo := Some(releaseRepo), //TODO define releaseRepo
       |  // Enables publishing to maven repo
       |  publishMavenStyle := true,
       |  packageDoc / publishArtifact := false,
       |  // disable using the Scala version in output paths and artifacts
       |  // crossPaths := false,
       |  // logLevel := Level.Debug,
       |)
       |
       |lazy val preventPublication = Seq(
       |  publish := {},
       |  publishArtifact := false,
       |  publishLocal := {}
       |)
       |// Other
       |
       |def loadingMessage = s\"\"\"Successfully started.
       |- Camundala: $$camundalaV
       |- Scala: $$scalaV
       |- Camunda: $$camundaV
       |  \"\"\"
       |
       |def generalSettings(module: Option[String] = None) = Seq(
       |  scalaVersion := scalaV,
       |  autoImportSetting(module),
       |  scalacOptions ++= Seq(
       |    "-Xmax-inlines:200" // is declared as erased, but is in fact used
       |    // "-Vprofile",
       |  )
       |)
       |
       |def autoImportSetting(module: Option[String] = None) =
       |  scalacOptions +=
       |    (module.toSeq.map(m => s"camundala.$$m") ++
       |      Seq(
       |        "java.lang", "java.time", "scala", "scala.Predef", "camundala.domain", "camundala.bpmn",
       |        "io.circe",
       |        "io.circe.generic.semiauto", "io.circe.derivation", "io.circe.syntax", "sttp.tapir",
       |        "sttp.tapir.json.circe"
       |      )).mkString(start = "-Yimports:", sep = ",", end = "")
       |
       |// dependencies
       |val typesafeConfigDep = "com.typesafe" % "config" % "1.4.3"
       |
       |lazy val bpmnDeps = Seq(
       |  "io.github.pme123" %% "camundala-bpmn" % camundalaV
       |)
       |lazy val apiDeps = Seq(
       |  "io.github.pme123" %% "camundala-api" % camundalaV,
       |  typesafeConfigDep
       |)
       |lazy val dmnDeps = Seq(
       |  "io.github.pme123" %% "camundala-dmn" % camundalaV
       |)
       |lazy val simulationDeps = Seq(
       |  "io.github.pme123" %% "camundala-simulation" % camundalaV
       |)
       |
       |lazy val workersDeps = Seq(
       |  "io.github.pme123" %% "camundala-camunda7-worker" % camundalaV,
       |)
       |
       |lazy val helperDeps = apiDeps ++ Seq(
       |  "io.github.pme123" %% "camundala-helper" % camundalaV,
       |)
       |
       |lazy val unitTestSettings = Seq(
       |  libraryDependencies += "org.scalameta" %% "munit" % mUnitVersion % Test,
       |  testFrameworks += new TestFramework("munit.Framework")
       |)
       |
       |""".stripMargin
  end buildSbt

  private lazy val pluginsSbt =
    s"""// $howToResetText
       |addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
       |
       |addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings" % "1.1.2")
       |addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.12.0")
       |
       |""".stripMargin

end CompanySbtGenerator
