package camundala.api.docs

case class DevStatisticsCreator(gitBasePath: os.Path, basePath: os.Path):

  def create(): Unit =
    val statistics =
      s"""{%
         |// auto generated - do not change!
         |helium.site.pageNavigation.depth = 1
         |%}
         |## Development Statistics
         |
         |Goes throw all projects and counts files and lines of code.
         |
         |${
          Seq("bpmn", "dmn", "groovy", "scala")
            .map(lineCount)
            .map { case ftc @ FileTypeCount(fileType, projectCounts) =>
              s"""
                 |${"*" * 20}
                 |
                 |File- and Line count for **$fileType** files:
                 |
                 |${projectCounts.map {
                  case ProjectCounts(project, fileCount, lineCount) =>
                    s" - $project: $lineCount of $fileCount Files"
                }.mkString("\n")}
                 |
                 |**Total** $fileType: **${ftc.lineCount}** of **${ftc.fileCount}** Files
                 |""".stripMargin
            }.mkString
        }""".stripMargin
    val path = basePath / "src" / "docs" / "devStatistics.md"
    os.write.over(path, statistics)
  end create

  private def lineCount(fileType: String): FileTypeCount =
    val projectCounts =
      os.list(gitBasePath)
        .filter(_.toIO.isDirectory)
        .filter(!_.toIO.isHidden)
        .map(_.baseName)
        .map(lineCountProject(fileType, _))
    FileTypeCount(fileType, projectCounts)
  end lineCount

  private def lineCountProject(fileType: String, project: String): ProjectCounts =
    val files = os.walk(gitBasePath / project)
      .filter(_.ext == fileType)
      .filter(_.toIO.isFile)
    val lineCounts = files
      .map(os.read.lines)
      .map(_.size)
      .sum
    ProjectCounts(project, files.size, lineCounts)

  end lineCountProject

  case class FileTypeCount(fileType: String, projectCounts: Seq[ProjectCounts]):
    lazy val lineCount =
      projectCounts.map(_.lineCount).sum
    lazy val fileCount =
      projectCounts.map(_.fileCount).sum
  end FileTypeCount

  case class ProjectCounts(project: String, fileCount: Int, lineCount: Int)

end DevStatisticsCreator
