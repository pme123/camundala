package camundala.helper.util

import camundala.api.docs.*

import scala.jdk.CollectionConverters.*

case class CompanyVersionHelper(
    companyName: String,
    repoSearch: (project: String, org: String) => String
):

  lazy val camundalaVersion: String =
    VersionHelper.repoSearchMavenCentral("camundala-api_3")

  lazy val companyCamundalaVersion: String =
    repoSearch(s"$companyName-camundala-api_3", companyName)

end CompanyVersionHelper

case class VersionHelper(
    projectConf: ApiProjectConf,
    repoSearch: (project: String, org: String) => String
):
  
  lazy val dependencyVersions: Map[String, String] =
    val deps = projectConf.dependencies
      .map:
        case dConf @ DependencyConf(org, name, version) =>
          val lastVersion = repoSearch(name, org)
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
  def repoSearchMavenCentral(project: String, org: String = "Not needed"): String =
    val searchResult = os.proc(
      "curl",
      s"https://search.maven.org/solrsearch/select?q=$project&rows=100&wt=json"
    ).call()
    val versionRegex = """"latestVersion":"(\d+\.\d+\.\d+)"""".r
    val maybeVersion =
      versionRegex.findFirstMatchIn(searchResult.out.text()).map(_.group(1))
    val version = maybeVersion.getOrElse:
      println(s"${Console.YELLOW_B}NOT FOUND!${Console.RESET} - Result from Maven Central:\n ${searchResult.out.text()}")
      s"VERSION"
    println(s"- Last Version of $project: $version")
    version
  end repoSearchMavenCentral
end VersionHelper
