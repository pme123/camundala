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
      apiProjectConfig: ApiProjectConfig
  ): DocProjectConfig =
    apply(apiProjectConfig, Seq.empty, DocProjectConfig.defaultVersion, false)

  def apply(
      apiProjectConfig: ApiProjectConfig,
      changelog: Seq[String],
      versionPrevious: String,
      isWorker: Boolean
  ): DocProjectConfig =
    DocProjectConfig(
      apiProjectConfig,
      versionPrevious,
      changelog,
      isWorker
    )
  end apply

end DocProjectConfig
