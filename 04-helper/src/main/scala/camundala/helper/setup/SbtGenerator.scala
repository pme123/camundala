package camundala.helper.setup

import camundala.helper.util.VersionHelper

case class SbtGenerator()(using
    config: SetupConfig
):

  lazy val generate: Unit =
    createOrUpdate(buildSbtDir, buildSbt)
    createOrUpdate(config.sbtProjectDir / "build.properties", buildProperties)
    createOrUpdate(config.sbtProjectDir / "plugins.sbt", pluginsSbt)
    createOrUpdate(config.sbtProjectDir / "ProjectDef.scala", projectDefSbt)
  end generate

  private lazy val projectConf = config.apiProjectConf
  private lazy val versionHelper = VersionHelper(projectConf, config.repoConfig.repoSearch)
  private lazy val buildSbtDir = config.projectDir / "build.sbt"

  private lazy val buildSbt =
    s"""// $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |import Settings.*
       |
       |ThisBuild / onLoadMessage := loadingMessage
       |ThisBuild / versionScheme := Some("semver-spec")
       |ThisBuild / libraryDependencySchemes += "io.github.pme123" %% "camundala-api" % "early-semver"
       |ThisBuild / evictionErrorLevel := Level.Warn
       |
       |val testFramework = "camundala.simulation.custom.SimulationTestFramework"
       |
       |$sbtRoot
       |$sbtModules
       |
       |""".stripMargin
  end buildSbt

  private lazy val buildProperties =
    s"""// $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |sbt.version=${config.versionConfig.sbtVersion}
       |""".stripMargin
  private lazy val pluginsSbt =
    s"""// $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
       |
       |// docker image
       |addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
       |
       |""".stripMargin

  private lazy val projectDefSbt =
    s"""// $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |import sbt.*
       |
       |object ProjectDef {
       |  val org = "${projectConf.org}"
       |  val name = "${projectConf.name}"
       |  val version = "${projectConf.version}"
       |  lazy val nameAsPackage = name.split("-").mkString(".")
       |
       |${versionHelper.dependencyVersionVars}
       |
       |${
        config.modules
          .filter(_.hasProjectDependencies)
          .map: moduleConfig =>
            s"""  lazy val ${moduleConfig.name}Dependencies = Seq(
               |    ${versionHelper.moduleDependencyVersions(
                moduleConfig.name,
                moduleConfig.projectDependenciesTestOnly
              )}
               |  )
               |""".stripMargin
          .mkString
      }
       |}""".stripMargin

  private lazy val sbtRoot =
    s"""
       |lazy val root = project
       |  .in(file("."))
       |  .settings(
       |    projectSettings(),
       |    publicationSettings, //Camunda artifacts
       |  ).aggregate(${config.modules.map(_.name).mkString(", ")})
       |""".stripMargin
  private lazy val sbtModules =
    config.modules
      .map: modC =>
        val name = modC.name
        val plugins = modC.sbtPlugins
        val sbtSettings = modC.sbtSettings
        val enablePlugins =
          if plugins.isEmpty then ""
          else plugins.mkString(".enablePlugins(", ", ", ")")
        s"""
           |lazy val $name = project
           |  .in(file("./${modC.nameWithLevel}"))
           |  .settings(
           |    projectSettings(Some("$name")),
           |    ${if modC.doPublish then "publicationSettings" else "preventPublication"},
           |    libraryDependencies ++= ${name}Deps${
            if sbtSettings.isEmpty then ""
            else sbtSettings.mkString(",\n    ", ",\n    ", "")
          }${if modC.hasTest then testSetting else ""}
           |  )${config.dependsOn(modC.level)}
           |  $enablePlugins
           |""".stripMargin
      .mkString

  private lazy val testSetting =
    """,
      |    libraryDependencies += mUnit,
      |    testFrameworks += new TestFramework(testFramework)""".stripMargin

end SbtGenerator
