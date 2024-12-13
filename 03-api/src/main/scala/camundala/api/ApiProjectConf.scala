package camundala.api

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try

case class ApiProjectConf(
    org: String,
    name: String,
    version: String,
    versionPrevious: String,
    dependencies: Seq[DependencyConf] = Seq.empty,
    changelog: Seq[String] = Seq.empty,
    isWorker: Boolean = false
):
  lazy val versionConf = versionFor(version)
  lazy val versionPreviousConf = versionFor(versionPrevious)

  lazy val versionAsInt = versionConf.versionAsInt

  lazy val isNew =
    versionConf.isMajor(versionPreviousConf) || versionConf.isMinor(versionPreviousConf)
  lazy val isPatched = !isNew && versionConf.isPatch(versionPreviousConf)
  lazy val minorVersion: String = versionConf.minorVersion
  lazy val fullName = s"$org:$name:$version"

  private def versionFor(version: String) =
    Try(ConfVersion(version))
      .getOrElse:
        println(s"Version $version is not valid. for $org:$name")
        ConfVersion(ApiProjectConf.defaultVersion)

end ApiProjectConf

object ApiProjectConf:
  lazy val defaultVersion = "0.1.0"

  def apply(
      packageFile: os.Path
  ): ApiProjectConf =
    apply(packageFile, Seq.empty, ApiProjectConf.defaultVersion, false)
  def apply(
      packageFile: os.Path,
      changelog: Seq[String],
      versionPrevious: String,
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
    ApiProjectConf(
      org,
      name,
      version,
      versionPrevious,
      dependencies.toSeq,
      changelog,
      isWorker
    )
  end apply

  def initDummy(projectName: String): ApiProjectConf =
    ApiProjectConf(
      projectName.split("-").head,
      projectName,
      ApiProjectConf.defaultVersion,
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

case class ConfVersion(major: Int, minor: Int, patch: Int):
  def isMajor(version: ConfVersion): Boolean =
    major != version.major

  def isMinor(version: ConfVersion): Boolean =
    major == version.major && minor != version.minor

  def isPatch(version: ConfVersion): Boolean =
    major == version.major && minor == version.minor && patch != version.patch

  lazy val minorVersion: String = s"$major.$minor"
  lazy val versionAsInt: Int =
    major * 100000 + minor * 1000 + patch
end ConfVersion

object ConfVersion:
  def apply(version: String): ConfVersion =
    version.split("\\.").map(_.toInt) match
      case Array(major, minor, patch) => ConfVersion(major, minor, patch)
end ConfVersion

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
