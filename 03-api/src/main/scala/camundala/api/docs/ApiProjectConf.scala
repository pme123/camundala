package camundala.api
package docs

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.CollectionHasAsScala

case class ApiProjectConf(
    org: String,
    name: String,
    version: String,
    versionPrevious: Option[String] = None,
    dependencies: Seq[DependencyConf] = Seq.empty,
    changelog: Seq[String] = Seq.empty,
    isNew: Boolean = false,
    isPatched: Boolean = false, 
    isWorker: Boolean = false
):
  lazy val versionAsInt = version.split("\\.") match
    case Array(major, minor, patch) =>
      major.toInt * 100000 + minor.toInt * 1000 + patch.toInt
      
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"
end ApiProjectConf

object ApiProjectConf:
  lazy val defaultVersion = "0.1.0-SNAPSHOT"

  def apply(
      packageFile: os.Path
  ): ApiProjectConf =
    apply(packageFile, Seq.empty, versionPrevious = None, false, false, false)
  def apply(
      packageFile: os.Path,
      changelog: Seq[String],
      versionPrevious: Option[String],
      isNew: Boolean,
      isPatched: Boolean,
        isWorker: Boolean
  ): ApiProjectConf =
    val conf = ConfigFactory.parseFile(packageFile.toIO)
    val org = conf.getString("org")
    val name = conf.getString("name")
    val version = conf.getString("version")
    val dependencies =
      conf
        .getObject("dependencies")
        .values()
        .asScala
        .map(v => DependencyConf.apply(v.render()))
    ApiProjectConf(org, name, version, versionPrevious, dependencies.toSeq, changelog, isNew, isPatched, isWorker)
  end apply

  def initDummy(projectName: String): ApiProjectConf =
    ApiProjectConf(
      projectName.split("-").head,
      projectName,
      ApiProjectConf.defaultVersion
    )
  end initDummy

  def init(projectName: String, newPackageFile: os.Path) =
    if !os.exists(newPackageFile)
    then
      println(s"Created initial $newPackageFile")
      os.makeDir.all(newPackageFile / os.up)
      os.write(
        newPackageFile,
        s"""
           |org = "${projectName.split("-").head}"
           |name = "$projectName"
           |version = "${ApiProjectConf.defaultVersion}"
           |dependencies: {
           |
           |}
           |""".stripMargin
      )
    end if
    apply(newPackageFile)
  end init

end ApiProjectConf

case class DependencyConf(
    org: String,
    name: String,
    version: String
):
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"
  lazy val projectPackage = s"$org.${name.split("-").filterNot(_ == org).mkString(".")}"
  def equalTo(packageConf: ApiProjectConf): Boolean =
    packageConf.org == org && packageConf.name == name && packageConf.minorVersion == minorVersion
end DependencyConf

object DependencyConf:
  def apply(dependency: String): DependencyConf =
    val dArray = dependency.replace("\"", "").split(":")
    DependencyConf(dArray(0), dArray(1), dArray(2))

end DependencyConf
