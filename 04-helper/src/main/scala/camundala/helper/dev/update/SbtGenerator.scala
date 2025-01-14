package camundala.helper.dev.update

import camundala.api.VersionHelper
import camundala.helper.util.TestType

case class SbtGenerator()(using
    config: DevConfig
):

  lazy val generate: Unit =
    createOrUpdate(buildSbtDir, buildSbt)
    generateBuildProperties()
    generatePluginsSbt
    createOrUpdate(config.sbtProjectDir / "ProjectDef.scala", projectDefSbt)
  end generate

  def generateBuildProperties(replaceStr: String = helperDoNotAdjustText) =
    createOrUpdate(config.sbtProjectDir / "build.properties", buildProperties(replaceStr))
  lazy val generatePluginsSbt =
    createOrUpdate(config.sbtProjectDir / "plugins.sbt", pluginsSbt)
  private lazy val projectConf = config.apiProjectConfig
  private lazy val versionHelper = VersionHelper(projectConf)
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

  private def buildProperties(replaceStr: String) =
    s"""$replaceStr
       |sbt.version=${config.versionConfig.sbtVersion}
       |""".stripMargin
  private lazy val pluginsSbt =
    s"""$helperDoNotAdjustText
       |addDependencyTreePlugin // sbt dependencyBrowseTreeHTML -> target/tree.html
       |
       |// docker image
       |addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.10.0")
       |
       |""".stripMargin

  private lazy val projectDefSbt =
    s"""$helperDoNotAdjustText
       |import sbt.*
       |
       |object ProjectDef {
       |  val org = "${projectConf.companyName}"
       |  val name = "${projectConf.projectName}"
       |  val version = "${projectConf.projectVersion}"
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
           |    Test / parallelExecution := false,
           |    testFrameworks += new TestFramework("camundala.simulation.custom.SimulationTestFramework")""".stripMargin
end SbtGenerator
