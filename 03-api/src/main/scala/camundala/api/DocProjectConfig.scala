package camundala.api

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.Try

case class DocProjectConfig(
    apiProjectConfig: ApiProjectConfig,
    versionPrevious: String,
    changelog: Seq[String] = Seq.empty,
    isWorker: Boolean = false
):
  lazy val companyName: String                = apiProjectConfig.companyName
  lazy val projectName: String                = apiProjectConfig.projectName
  lazy val projectVersion: VersionConfig      = apiProjectConfig.projectVersion
  lazy val versionPreviousConf: VersionConfig = versionFor(versionPrevious)
  lazy val version: String                    = projectVersion.toString
  lazy val versionAsInt: Int                  = projectVersion.versionAsInt

  lazy val dependencies: Seq[DependencyConfig] = apiProjectConfig.dependencies

  lazy val isNew                =
    projectVersion.isMajor(versionPreviousConf) || projectVersion.isMinor(versionPreviousConf)
  lazy val isPatched            = !isNew && projectVersion.isPatch(versionPreviousConf)
  lazy val minorVersion: String = projectVersion.minorVersion
  lazy val fullName             = s"$companyName:$projectName:$projectVersion"

  private def versionFor(version: String) =
    Try(VersionConfig(version))
      .getOrElse:
        println(s"Version $version is not valid. for $companyName:$projectName")
        VersionConfig(DocProjectConfig.defaultVersion)

end DocProjectConfig

object DocProjectConfig:
  lazy val defaultVersion = "0.1.0"

  def apply(
      packageFile: os.Path
  ): DocProjectConfig =
    apply(packageFile, Seq.empty, DocProjectConfig.defaultVersion, false)
  def apply(
      packageFile: os.Path,
      changelog: Seq[String],
      versionPrevious: String,
      isWorker: Boolean
  ): DocProjectConfig =
    val apiProjectConfig = ApiProjectConfig(packageFile)
    DocProjectConfig(
      apiProjectConfig,
      versionPrevious,
      changelog,
      isWorker
    )
  end apply

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
           |version = "${DocProjectConfig.defaultVersion}"
           |dependencies: {
           |
           |}
           |""".stripMargin
      )
    end if
    apply(newPackageFile)
  end init

end DocProjectConfig
