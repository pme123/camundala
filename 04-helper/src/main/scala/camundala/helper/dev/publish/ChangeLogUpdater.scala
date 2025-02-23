package camundala.helper.dev.publish

import java.time.LocalDate
import scala.language.postfixOps

case class ChangeLogUpdater(
    newVersion: String,
    // standard for Github
    commitsAddressUpdater: String => String
):

  private def checkForNewCommits: Boolean =
    if
      (!lastVersion.contains(newVersion) && gitLogNew.nonEmpty) || changeLog
        .contains("---DRAFT")
    then
      println(s"Commits Address: $commitsAddress")
      println(s"lastDate: $lastDate")
      println(s"newVersion: $newVersion")
      println(s"lastVersion: $lastVersion - $lastVersionLine")
      println(s"Missing Commits\n: $gitLogNew")
      os.write.over(os.pwd / "CHANGELOG.md", newChangeLog)
      true
    else false

  private lazy val changeLog = os.read(os.pwd / "CHANGELOG.md")

  private lazy val regex                           = """## (\d+\.\d+\.\d+) - (\d\d\d\d-\d\d-\d\d).*""".r
  private lazy val firstMatch                      = regex.findFirstMatchIn(changeLog)
  private lazy val lastVersion: Option[String]     = firstMatch.map(_.group(1))
  private lazy val lastVersionInt: Int             = lastVersion
    .map { v =>
      val vArray = v.split('.')
      vArray.head.toInt * 1000 * 1000 + vArray.tail.head.toInt * 1000 + vArray.last.toInt
    }
    .getOrElse(0)
  private lazy val lastDate: Option[String]        = firstMatch.map(_.group(2))
  private lazy val lastVersionLine: Option[String] = firstMatch.map(_.group(0))
  private lazy val gitLog                          = os
    .proc(
      "git",
      "log",
      "--date=format:%Y-%m-%d",
      "--pretty=format:%cd :: %H :: %s"
    )
    .call()
  private lazy val commitsAddress                  =
    val remote: String = os
      .proc("git", "config", "--get", "remote.origin.url")
      .call()
      .out
      .lines()
      .head
    println(s"REMOTE: $remote")
    commitsAddressUpdater(remote)
  end commitsAddress

  private lazy val gitLogNew = gitLog.out
    .lines()
    .map(_.split("::").map(_.trim).toSeq)
    .filterNot(_.last.startsWith("Init new Version"))
    .takeWhile(r => lastDate.forall(_.compareTo(r.head) <= 0))
    .foldLeft("")(createNewChangelogEntries)

  private def createNewChangelogEntries(
      result: String,
      newLine: Seq[String]
  ) =
    result +
      (if newLine.last.startsWith("Released Version") then
         val vArray        = newLine.last.split(" ").last.split('.')
         val newVersionInt =
           vArray.head.toInt * 1000 * 1000 + vArray.tail.head.toInt * 1000 + vArray.last.toInt
         println(s"VERSION new: $newVersionInt - last: $lastVersionInt")
         if newVersionInt.compareTo(lastVersionInt) > 0 then
           s"\n\n## ${newLine.last.replace("Released Version ", "")} - ${newLine.head}\n### Changed"
         else
           "\nEXISTING VERSION" // flag for next lines that this version is already in the Changelog
       else if result.endsWith("\nEXISTING VERSION") then
         "" // skip all entries for an existing version
       else
         s"\n- ${newLine.last} - see [Commit]($commitsAddress${newLine.tail.head})")
  end createNewChangelogEntries

  private lazy val newChangeLog = lastVersionLine
    .map(l =>
      changeLog.replace(
        l,
        s"""
           |//---DRAFT start
           |## $newVersion - ${LocalDate.now()}
           |### Changed ${gitLogNew.mkString.replace("\nEXISTING VERSION", "")}
           |//---DRAFT end
           |
           |$l""".stripMargin
      )
    )
    .getOrElse(s"""|$changeLog
                   |
                   |//---DRAFT start
                   |## $newVersion - ${LocalDate.now()}
                   |### Changed ${gitLogNew.mkString}
                   |//---DRAFT end
                   |""".stripMargin)
  end newChangeLog
end ChangeLogUpdater

object ChangeLogUpdater:
  def verifyChangelog(
      newVersion: String,
      // standard for Github
      commitsAddress: String => String = _.replace(".git", "/commit/")
  ): Unit =
    if ChangeLogUpdater(newVersion, commitsAddress).checkForNewCommits then
      throw new IllegalStateException(
        "The CHANGELOG is still in a DRAFT version! Please adjust CHANGELOG.md"
      )
  end verifyChangelog

end ChangeLogUpdater
