package camundala.api

import camundala.api.{ApiProjectConfig, DependencyConfig}

import scala.jdk.CollectionConverters.*

case class VersionHelper(
    projectConf: ApiProjectConfig
):

  lazy val dependencyVersions: Map[String, String] =
    val deps = projectConf.dependencies
      .map:
        case dConf @ DependencyConfig(name, version) =>
          val lastVersion = VersionHelper.repoSearch(name, dConf.companyName)
          if !lastVersion.startsWith(dConf.minorVersion) then
            println(
              s"${Console.YELLOW_B}WARNING: There is a newer Version for ${dConf.companyName}:$name:$version -> $lastVersion${Console.RESET}"
            )
          name -> lastVersion
      .toMap
    if deps.nonEmpty then
      println("Dependencies:")

    deps.foreach:
      case name -> lastVersion =>
        println(s"- $name -> $lastVersion")
    deps
  end dependencyVersions

  lazy val dependencyVersionVars: String =
    dependencyVersions
      .map:
        case name -> lastVersion =>
          s"""  lazy val ${variableName(name)}Version = "$lastVersion""""
      .toSeq.sorted.mkString("\n")

  def moduleDependencyVersions(moduleName: String, testOnly: Boolean): String =
    dependencyVersions
      .map:
        case name -> _ =>
          s""""${projectConf.companyName}" % "$name-$moduleName" % ${variableName(name)}Version${
              if testOnly then " % Test" else ""
            }"""
      .toSeq.sorted.mkString(",\n    ")

  private def variableName(name: String) = // company-project -> companyProject
    name.split("-").toList match
      case head :: tail =>
        head + tail.map(n => n.head.toUpper + n.tail).mkString
      case other        => other

end VersionHelper

object VersionHelper:

  lazy val camundalaVersion: String =
    repoSearch("camundala-domain_3", "io.github.pme123")

  // this expects a projectName in this pattern mycompany-myproject
  def repoSearch(projectName: String): String =
    repoSearch(projectName, projectName.split("-").head)

  def repoSearch(project: String, org: String): String =
    val searchResult = os.proc(
      "cs",
      "complete-dep",
      s"$org:$project:"
    ).call()
    val versionRegex = """(\d+\.\d+\.\d+)""".r
    val versionStr   =
      searchResult.out.text()
        .split("\n").toList
        .filter(_.trim.matches(versionRegex.regex))
        .map: v =>
          val vers = v.split("\\.")
            .filterNot(_.trim.toIntOption.isEmpty)
            .map: nr =>
              String.format("%03d", nr.trim.toInt)
            .foldLeft("")(_ + _)
          v -> vers
        .sortBy(_._2)(Ordering[String].reverse)
        .headOption
        .map(_._1)
    val version      = versionStr.getOrElse:
      println(
        s"${Console.YELLOW_B}NOT FOUND!${Console.RESET} - Result from Maven Central:\n ${searchResult.out.text()}" +
          s"Check: cs complete-dep $org:$project:"
      )
      s"0.0.0"

    println(s"- Last Version of $org:$project: $version")
    version
  end repoSearch

end VersionHelper
