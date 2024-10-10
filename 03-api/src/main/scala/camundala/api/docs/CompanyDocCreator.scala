package camundala.api
package docs

import os.Path

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** the idea is use Camundala to create Company's Process documentation.
  *
  * For a Start you can create a Catalog.
  */
trait CompanyDocCreator extends DependencyCreator:

  protected def gitBasePath: os.Path = apiConfig.projectsConfig.gitDir
  protected given configs: Seq[ApiProjectConf] = setupConfigs()
  lazy val projectConfigs: Seq[ProjectConfig] =
    apiConfig.projectsConfig.projectConfigs

  protected def upload(releaseTag: String): Unit

  def prepareDocs(): Unit =
    println(s"API Config: $apiConfig")
    apiConfig.projectsConfig.init
    createCatalog()
    DevStatisticsCreator(gitBasePath, apiConfig.basePath).create()
    createDynamicConf()
    // println(s"Preparing Docs Started")
    createReleasePage()
  end prepareDocs

  def releaseDocs(): Unit =
    createDynamicConf()
    println(s"Releasing Docs started")
    os.proc(
      "sbt",
      "-J-Xmx3G",
      "clean",
      "laikaSite" // generate HTML pages from Markup
    ).callOnConsole()
    upload(releaseConfig.releaseTag)
  end releaseDocs

  protected def createCatalog(): Unit =

    val catalogs = s"""{%
                      |// auto generated - do not change!
                      |helium.site.pageNavigation.depth = 1
                      |%}
                      |## Catalog
                      |${projectConfigs
                       .map { case pc @ ProjectConfig(projectName, _, _, _, _) =>
                         val path = pc.absGitPath(gitBasePath) / catalogFileName
                         if os.exists(path) then
                           os.read(path)
                         else
                           s"""### $projectName
                              |Sorry there is no $path.
                              |""".stripMargin
                         end if
                       }
                       .mkString("\n")}""".stripMargin
    val catalogPath = apiConfig.basePath / "src" / "docs" / catalogFileName
    println(s"Catalog Path: $catalogPath")
    if os.exists(catalogPath) then
      os.write.over(catalogPath, catalogs)
    else
      os.write(catalogPath, catalogs, createFolders = true)
  end createCatalog

  protected def createDynamicConf(): Unit =
    val table =
      s"""// auto generated - do not change!
         |laika.versioned = false
         |release.tag = "${releaseConfig.releaseTag}"
         |created.day = "${LocalDate
          .now()
          .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
         |laika.navigationOrder = [
         |  index.md
         |  release.md
         |  overviewDependencies.md
         |  pattern.md
         |  statistics.md
         |  devStatistics.md
         |  catalog.md
         |  contact.md
         |  instructions.md
         |  dependencies
         |]
         """.stripMargin
    os.write.over(apiConfig.basePath / "src" / "docs" / "directory.conf", table)
  end createDynamicConf

  private def createReleasePage(): Unit =
    given configs: Seq[ApiProjectConf] = setupConfigs()
    given ReleaseConfig = releaseConfig
    DependencyValidator().validateDependencies
    val indexGraph = DependencyGraphCreator().createIndex
    DependencyLinkCreator().createIndex(indexGraph)
    val dependencyGraph = DependencyGraphCreator().createDependencies
    DependencyLinkCreator().createDependencies(dependencyGraph)
    DependencyGraphCreator().createProjectDependencies
    val releaseNotes = setupReleaseNotes
    DependencyValidator().validateOrphans
    val tableFooter = "(\\*) New in this Release / (\\*\\*) Patched in this Release - check below for the details"
    val table =
      s"""
         |{%
         |laika.versioned = true
         |%}
         |
         |# Release ${releaseConfig.releaseTag}
         | ${releaseConfig.releasedLabel}
         |
         |${releaseConfig.jiraReleaseUrl.map(u => s"[JIRA Release Planing]($u)").getOrElse("")}
         |
         |## Camunda Dependencies
         |
         |${dependencyTable(configs, isWorker = false)}
         |
         |$tableFooter
         |
         |## Worker Dependencies
         |
         |${dependencyTable(configs, isWorker = true)}
         |
         |$tableFooter
         |
         |$releaseNotes
         """.stripMargin

    val releasePath = apiConfig.basePath / "src" / "docs" / "release.md"
    os.write.over(releasePath, table)
  end createReleasePage

  private def setupConfigs(): Seq[ApiProjectConf] =
    val configsLines = os.read.lines(apiConfig.basePath / "VERSIONS.conf")
    val configsPreviousPath = apiConfig.basePath / "VERSIONS_PREVIOUS.conf"
    val previousVersionsLines =
      if os.exists(configsPreviousPath)
      then Some(os.read.lines(configsPreviousPath))
      else None

    val versions = extractVersions(configsLines)
    val previousVersions = previousVersionsLines.map(extractVersions)

    versions
      .toSeq.flatMap:
        case projectName -> (version, isNew, isPatched) =>
          val previousVersion = previousVersions.flatMap(_.get(projectName)).map(_._1)
          fetchConf(
            projectName.replace("-worker", ""),
            version,
            previousVersion,
            isNew,
            isPatched,
            projectName.endsWith("worker")
          )

  end setupConfigs

  private def extractVersions(
      versions: Seq[String]
  ): Map[String, (String, Boolean, Boolean)] =
    versions
      .filter(l => l.contains("Version") && !l.contains("\"\""))
      .map: l =>
        val projectName = l.trim
          .split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")
          .map(_.toLowerCase)
          .takeWhile(!_.startsWith("version"))
          .mkString("-")
        val regex = """"(\d+\.\d+\.\d+)"""".r
        val version = regex.findFirstMatchIn(l.trim).get.group(1)
        val isNew = l.toLowerCase().contains("// new")
        val isPatched = l.toLowerCase().contains("// patched")
        projectName -> (version, isNew, isPatched)
      .toMap

  private def fetchConf(
      project: String,
      version: String,
      versionPrevious: Option[String],
      isNew: Boolean,
      isPatched: Boolean,
      isWorker: Boolean
  ) =
    for
      projConfig <- apiConfig.projectsConfig.projectConfig(project)
      projectPath = projConfig.absGitPath(gitBasePath)
      _ = println(s"Project Git Path $projectPath")
      _ = os.makeDir.all(projectPath)
      _ = os.proc("git", "fetch", "--tags").callOnConsole(projectPath)
      _ = os
        .proc("git", "checkout", s"tags/v$version")
        .callOnConsole(projectPath)
    yield ApiProjectConf(
      projectPath / apiConfig.projectsConfig.projectConfPath,
      os.read.lines(projectPath / "CHANGELOG.md"),
      versionPrevious,
      isNew,
      isPatched,
      isWorker
    )

  private def dependencyTable(configs: Seq[ApiProjectConf], isWorker: Boolean) =
    val filteredConfigs =
      configs
        .filter(_.isWorker == isWorker)
        .filter(c =>
          configs.exists(c2 =>
            c.name != c2.name && c2.dependencies.exists(d => d.name == c.name)
          ) // && c.version.matches(d.version)) )
        )
        .sortBy(_.name)

    "| **Package** | **Version** " +
      filteredConfigs
        .map(c => s"**${c.name}** ${c.version}")
        .mkString("| ", " | ", " |") +
      "\n||----| :----:  " + filteredConfigs
        .map(_ => s":----:")
        .mkString("| ", " | ", " |\n") +
      filteredConfigs
        .sortBy(_.name)
        .map { c =>
          val (name, version) =
            c match
              case _ if c.isNew =>
                (s"[${c.name}]*", s"**${c.version}**")
              case _ if c.isPatched =>
                (s"[${c.name}]**", s"_${c.version}_")
              case _ =>
                (s"${c.name}", s"${c.version}")

          s"|| **$name** | $version " +
            filteredConfigs
              .map(c2 =>
                c.dependencies
                  .find { c3 =>
                    val version2 =
                      c2.version.split("\\.").take(2).mkString("", ".", ".")
                    c3.name == c2.name && c3.version.startsWith(version2)
                  }
                  .map(_ => c2.version)
                  .getOrElse("")
              )
              .mkString("| ", " | ", " |")
        }
        .mkString("\n")
  end dependencyTable

  private def setupReleaseNotes(using configs: Seq[ApiProjectConf]) =
    val mergeConfigs = configs
      .filter(c => c.isNew || c.isPatched) // take only the new or patched ones
      .foldLeft(Seq.empty[ApiProjectConf]):
        case (result, c) =>
          result
            .find(_.name == c.name)
            .map: mc =>
              if c.versionAsInt > mc.versionAsInt
              then result.filterNot(_.name == c.name) :+ c
              else result
            .getOrElse(result :+ c)

    val projectChangelogs = mergeConfigs
      .sortBy(_.name)
      .map(c => s"""
                   |## [${c.name}](${apiConfig.docProjectUrl(c.name)}/OpenApi.html)
                   |${extractChangelog(c)}
                   |""".stripMargin)
      .mkString("\n")
    s"""
       |# Release Notes
       |# DRAFT!!!
       |
       |_Automatically gathered from the project's Change Logs and manually adjusted.
       |Please check the project _CHANGELOG_ for more details._
       |
       |${releaseConfig.releaseNotes}
       |
       |${projectChangelogs}
       |
       |""".stripMargin
  end setupReleaseNotes

  private def extractChangelog(conf: ApiProjectConf) =
    val versionRegex = "## \\d+\\.\\d+\\.\\d+.+"
    val groups = ChangeLogGroup.values

    val changeLogEntries = conf.changelog
      // start with the release version
      .dropWhile(!_.trim.startsWith(s"## ${conf.version}"))
      // take only to the ones that belong to this version
      .takeWhile(l =>
        conf.versionPrevious
          .map: v =>
            !l.trim.startsWith(s"## $v")
          .getOrElse:
            !(l.matches(versionRegex) && !l.startsWith(s"## ${conf.minorVersion}"))
      )
      // remove all version titles
      .filterNot(_.matches(versionRegex))
      // remove empty lines
      .filter(_.trim.nonEmpty)
      // group
      .foldLeft((Seq.empty[ChangeLogEntry], ChangeLogGroup.Changed)) {
        case ((entries, activeGroup), line) =>
          line match
            case l if l.startsWith("### ") => // group
              val group = ChangeLogGroup.withName(l.drop(4).trim)
              (entries, group)
            case l =>
              val regex = """(.*)(MAP-\d+)(:? )(.*)""".r
              val newEntry = regex.findFirstMatchIn(l) match
                case Some(v) =>
                  val jiraTicket = v.group(2)
                  ChangeLogEntry(
                    activeGroup,
                    s"- ${v.group(4)}",
                    Some(jiraTicket)
                  )
                case None => ChangeLogEntry(activeGroup, l)
              (entries :+ newEntry, activeGroup)
      }
      ._1

    val preparedGroups = groups
      // take only the groups that have entries
      .filter(g => changeLogEntries.exists(_.group == g))
      .foldLeft(Map.empty[ChangeLogGroup.Value, Map[String, Seq[String]]]) {
        case (result, group) =>
          // get the new entries for a group - group it by ticket
          val newEntries =
            changeLogEntries.filter(_.group == group).groupBy(_.ticket).map {
              case k -> v => k.getOrElse("Other") -> v.map(_.text)
            }
          if groups.take(3).contains(group) then // for Added, Changed, Fixed -> merge tickets
            val existingResult = newEntries.foldLeft(result) {
              case (result, (ticket, texts)) =>
                (result.view.mapValues { rTickets =>
                  rTickets.map {
                    case rT -> rTexts if rT == ticket =>
                      rT -> (rTexts ++ texts)
                    case rT -> rTexts =>
                      rT -> rTexts
                  }
                }.toMap)

            }
            // only take new entries that were not merged
            val filteredNew = newEntries.filter { case t -> _ =>
              !existingResult.values.flatten.exists(_._1 == t)
            }

            existingResult + (group -> filteredNew)
          else
            result + (group -> newEntries)
          end if
      }
      .filter(g => g._2.nonEmpty) // remove if there are no new entries

    preparedGroups
      .map { case k -> v =>
        s"""### $k
           |${v.toSeq
            .sortBy(_._1)
            .map { case ticket -> entries =>
              s"""
                 |**${replaceJira(ticket)}**
                 |
                 |${entries.mkString("\n")}
                 |""".stripMargin
            }
            .mkString("\n")}
           |""".stripMargin
      }
      .mkString("\n")
  end extractChangelog

  private def replaceJira(
      jiraTicket: String
  ): String =
    if jiraTicket == "Other" then
      jiraTicket
    else
      s"[$jiraTicket](https://issue.swisscom.ch/browse/$jiraTicket)"

  private object ChangeLogGroup extends Enumeration:
    val Added, Changed, Fixed, Deprecated, Removed, Security = Value

  private case class ChangeLogEntry(
      group: ChangeLogGroup.Value = ChangeLogGroup.Changed,
      text: String,
      ticket: Option[String] = None
  )

end CompanyDocCreator
