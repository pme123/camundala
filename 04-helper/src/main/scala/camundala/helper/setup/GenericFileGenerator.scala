package camundala.helper.setup

import camundala.helper.util.VersionHelper

case class GenericFileGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(config.projectDir / ".scalafmt.conf", scalafmt)
    createOrUpdate(config.projectDir / ".gitignore", gitignore)
    createOrUpdate(config.projectDir / "helper.scala", helperScala)
    os.proc("chmod", "+x", config.projectDir / "helper.scala").call()
    createIfNotExists(config.projectDir / "CHANGELOG.md", changeLog)
    os.makeDir.all(config.projectDir / ".run")
    os.makeDir.all(config.projectDir / ".vscode")
    createOrUpdate(config.projectDir / ".run" / "WorkerTestApp.run.xml", workerTestAppIntellij)
    createOrUpdate(config.projectDir / ".vscode" / "launch.json", workerTestAppVsCode)
  end generate

  private lazy val scalafmt =
    s"""# $doNotAdjust. This file is replaced by `./helper.scala update`.
       |
       |version = "3.7.15"
       |project.git = true
       |runner.dialect = scala3
       |align.preset = none
       |align.stripMargin = true
       |assumeStandardLibraryStripMargin = true
       |binPack.literalsExclude = ["Term.Name"]
       |
       |maxColumn = 100 // For my wide 30" display.
       |# Recommended, to not penalize `match` statements
       |indent.matchSite = 0
       |
       |newlines.source = keep
       |rewrite.scala3.convertToNewSyntax = true
       |rewrite.scala3.removeOptionalBraces = yes
       |rewrite.scala3.insertEndMarkerMinLines = 5
       |
       |fileOverride {
       |  "glob:**/project/**" {
       |    runner.dialect = scala213
       |  }
       |  "glob:**/build.sbt" {
       |    runner.dialect = scala213
       |  }
       |}
       |""".stripMargin

  private lazy val gitignore =
    s"""# $doNotAdjust. This file is replaced by `./helper.scala update`.
       |*.class
       |*.log
       |
       |target
       |/project/project
       |/project/target
       |/project/.*
       |.cache
       |.classpath
       |.project
       |.settings
       |bin
       |/.idea/
       |/.sbt/
       |/.ivy2/
       |/.g8/
       |/project/metals.sbt
       |/.bloop/
       |/.ammonite/
       |/.metals/
       |/.scala-build/
       |/.vscode/settings.json
       |/.templUpdate/
       |/.camunda/element-templates/dependencies/
       |
       |/.bsp/
       |/**/.generated/
       |/**/.gradle/
       |/**/build/
       |/**/gradle*
       |test.*
       |
       |""".stripMargin

  private val helperScala = ScriptCreator()
    .projectHelper

  private lazy val changeLog =
    s"""# Changelog
       |
       |All notable changes to this project will be documented in this file.
       |
       |* Types of Changes (L3):
       |  * Added: new features
       |  * Changed: changes in existing functionality
       |  * Deprecated: soon-to-be-removed features
       |  * Removed: now removed features
       |  * Fixed: any bug fixes
       |  * Security: in case of vulnerabilities
       |
       |
       |The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
       |and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
       |
       |""".stripMargin

  private lazy val workerTestAppIntellij =
    s"""|<!-- DO NOT ADJUST. This file is replaced by `./helper.scala update` -->
        |<component name="ProjectRunConfigurationManager">
        |  <configuration default="false" name="WorkerTestApp" type="Application" factoryName="Application" nameIsGenerated="true">
        |    <envs>
        |      <env name="BPF_GATEWAY_URL" value="https://localhost:51051" />
        |      <env name="FSSO_BASE_URL" value="http://host.lima.internal:8090/auth" />
        |      <env name="WORKER_TEST_MODE" value="true" />
        |    </envs>
        |    <option name="MAIN_CLASS_NAME" value="${config.projectPackage}.worker.WorkerTestApp" />
        |    <module name="${config.projectName}.${config.projectName}-worker" />
        |    <extension name="coverage">
        |      <pattern>
        |        <option name="PATTERN" value="${config.projectPackage}.worker.*" />
        |        <option name="ENABLED" value="true" />
        |      </pattern>
        |    </extension>
        |    <method v="2">
        |      <option name="Make" enabled="true" />
        |    </method>
        |  </configuration>
        |</component>
        |""".stripMargin
  private lazy val workerTestAppVsCode =
    s"""|// DO NOT ADJUST. This file is replaced by `./helper.scala update`.
        |{
        |    "version": "2.0.0",
        |    "configurations": [
        |
        |        {
        |            "type": "scala",
        |            "request": "launch",
        |            "name": "WorkerTestApp",
        |            "mainClass": "${config.projectPackage}.worker.WorkerTestApp",
        |            "args": [],
        |            "jvmOptions": [],
        |            "env": { "BPF_GATEWAY_URL": "https://localhost:51051", "FSSO_BASE_URL": "http://host.lima.internal:8090/auth", "WORKER_TEST_MODE": "true"},
        |        }
        |    ]
        |}
        |""".stripMargin
end GenericFileGenerator
