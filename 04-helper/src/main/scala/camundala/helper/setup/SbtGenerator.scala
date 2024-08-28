package camundala.helper.setup

import camundala.helper.util.VersionHelper

case class SbtGenerator()(using
    config: SetupConfig
):

  lazy val generate: Unit =
    createOrUpdate(buildSbtDir, buildSbt)
    generateBuildProperties
    generatePluginsSbt
    createOrUpdate(config.sbtProjectDir / "ProjectDef.scala", projectDefSbt)
  end generate

  lazy val generateBuildProperties =
    createOrUpdate(config.sbtProjectDir / "build.properties", buildProperties)
  lazy val generatePluginsSbt =
    createOrUpdate(config.sbtProjectDir / "plugins.sbt", pluginsSbt)
  private lazy val projectConf = config.apiProjectConf
  private lazy val versionHelper = VersionHelper(projectConf, config.reposConfig.repoSearch)
  private lazy val buildSbtDir = config.projectDir / "build.sbt"

  private lazy val buildSbt =
    s"""// $doNotAdjust. This file is replaced by `./helper.scala update`.
       |import Settings.*
       |
       |ThisBuild / onLoadMessage := loadingMessage
       |ThisBuild / versionScheme := Some("semver-spec")
       |ThisBuild / libraryDependencySchemes += "io.github.pme123" %% "camundala-api" % "early-semver"
       |ThisBuild / evictionErrorLevel := Level.Warn
       |ThisBuild / usePipelining := true
       |
       |$sbtRoot
       |$sbtModules
       |
       |""".stripMargin
  end buildSbt

  private lazy val buildProperties =
    s"""// $doNotAdjust. This file is replaced by `./helper.scala update`.
       |sbt.version=${config.versionConfig.sbtVersion}
       |""".stripMargin
  private lazy val pluginsSbt =
    s"""// $doNotAdjust. This file is replaced by `./helper.scala update`.
       |addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
       |
       |// docker image
       |addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
       |
       |""".stripMargin

  private lazy val projectDefSbt =
    s"""// $doNotAdjust. This file is replaced by `./helper.scala update`.
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
       |    sourcesInBase := false,
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
        def sbtSubProjectName(subProject: String) =
          name + subProject.head.toUpper + subProject.tail

        val (subProjects, aggregateSubProjects) =
          if modC.generateSubModule then
            config.subProjects
              .map: sp =>
                s"""lazy val ${sbtSubProjectName(sp)} = project
                   |  .in(file("${modC.nameWithLevel}/$sp"))
                   |  .settings(
                   |    projectSettings(Some("$name-$sp"), Some("$name")),
                   |    publicationSettings${testSetting(modC)}
                   |  )
                   |  .dependsOn(${name}Base)
                   |""".stripMargin
              .mkString ->
              s""".aggregate(${
                  if config.subProjects.nonEmpty
                  then
                    config.subProjects.map(sbtSubProjectName)
                      .mkString(s"${name}Base, ", ", ", "")
                  else ""
                })
                 |  .dependsOn(${config.subProjects.map(sbtSubProjectName).mkString(", ")})
                 |
                 |${
                  if config.subProjects.nonEmpty
                  then s"""lazy val ${name}Base = project
                          |  .in(file("${modC.nameWithLevel}/_base"))
                          |  .settings(
                          |    projectSettings(Some("$name-base"), Some("$name")),
                          |    publicationSettings,
                          |    libraryDependencies ++= ${name}Deps,
                          |    testSettings
                          |  )""".stripMargin
                  else ""
                }""".stripMargin
          else "" -> ""
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
          }${testSetting(modC)}
           |  )${config.dependsOn(modC.level)}
           |  $aggregateSubProjects
           |  $enablePlugins
           |$subProjects
           |""".stripMargin
      .mkString

  private def testSetting(modC: ModuleConfig) =
    modC.testType match
      case TestType.None => ""
      case TestType.MUnit =>
        s""",
           |    testSettings""".stripMargin
      case TestType.Simulation =>
        s""",
           |    Test / parallelExecution := true,
           |    testFrameworks += new TestFramework("camundala.simulation.custom.SimulationTestFramework")""".stripMargin
end SbtGenerator
