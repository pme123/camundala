package camundala.helper.setup

import camundala.helper.util.VersionHelper

case class GenericFileGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(config.projectDir / ".scalafmt.conf", scalafmt)
    createOrUpdate(config.projectDir / ".gitignore", gitignore)
    createOrUpdate(config.projectDir / "helper.sc", helperSc)
    createOrUpdate(config.projectDir / "CHANGELOG.md", changeLog)
  end generate

  private lazy val scalafmt =
    s"""# $doNotAdjust. This file is replaced by `amm helper.sc update`.
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
    s"""# $doNotAdjust. This file is replaced by `amm helper.sc update`.
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
       |/.ivy2/
       |/.g8/
       |/project/metals.sbt
       |/.bloop/
       |/.ammonite/
       |/.metals/
       |/.vscode/
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

  private val helperSc = ScriptCreator()
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
end GenericFileGenerator
