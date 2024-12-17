package camundala.helper.util

import camundala.api.ApiProjectConf
import camundala.api.DependencyConf

import scala.jdk.CollectionConverters.*

case class CompanyVersionHelper(
    companyName: String,
):
  
  lazy val companyCamundalaVersion: String =
    VersionHelper.repoSearch(s"$companyName-camundala-api_3", companyName)

end CompanyVersionHelper

case class VersionHelper(
    projectConf: ApiProjectConf,
):

  lazy val dependencyVersions: Map[String, String] =
    val deps = projectConf.dependencies
      .map:
        case dConf @ DependencyConf(org, name, version) =>
          val lastVersion = VersionHelper.repoSearch(name, org)
          if !lastVersion.startsWith(dConf.minorVersion) then
            println(
              s"${Console.YELLOW_B}WARNING: There is a newer Version for $org:$name:$version -> $lastVersion${Console.RESET}"
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
          s""""${projectConf.org}" % "$name-$moduleName" % ${variableName(name)}Version${
              if testOnly then " % Test" else ""
            }"""
      .toSeq.sorted.mkString(",\n    ")

  private def variableName(name: String) = // company-project -> companyProject
    name.split("-").toList match
      case head :: tail =>
        head + tail.map(n => n.head.toUpper + n.tail).mkString
      case other => other

end VersionHelper

object VersionHelper:

  lazy val camundalaVersion: String =
    repoSearch("camundala-api_3", "io.github.pme123")

  def repoSearch(project: String, org: String): String =
    val searchResult = os.proc(
      "cs",
      "complete-dep",
      s"$org:$project:"
    ).call()
    val versionRegex = """(\d+\.\d+\.\d+)""".r
    val versionStr =
      searchResult.out.text()
        .split("\n").toList
        .filter(_.trim.matches(versionRegex.regex))
        .map:v =>
          val vers = v.split("\\.")
            .filterNot(_.trim.toIntOption.isEmpty)
            .map: nr =>
              String.format("%03d", nr.trim.toInt)
            .foldLeft("")(_ + _)
          v -> vers
        .sortBy(_._2)(Ordering[String].reverse)
        .headOption
        .map(_._1)
    val version = versionStr.getOrElse:
      println(
        s"${Console.YELLOW_B}NOT FOUND!${Console.RESET} - Result from Maven Central:\n ${searchResult.out.text()}" +
          s"Check: cs complete-dep $org:$project:"
      )
      s"VERSION NOT FOUND"

    println(s"- Last Version of $org:$project: $version")
    version
  end repoSearch

end VersionHelper
