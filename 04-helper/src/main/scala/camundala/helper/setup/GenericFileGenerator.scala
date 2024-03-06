package camundala.helper.setup

import camundala.helper.util.VersionHelper

case class GenericFileGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(config.projectDir / ".scalafmt.conf", scalafmt)
    createOrUpdate(config.projectDir / ".gitignore", gitignore)
    createOrUpdate(config.projectDir / "helper.sc", helperSc)
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
       |*/project/project
       |project/target
       |.cache
       |.classpath
       |.project
       |.settings
       |bin
       |/.idea/
       |/.g8/
       |/project/metals.sbt
       |/.bloop/
       |/.metals/
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
    .projectHelper(config.projectName)

end GenericFileGenerator
