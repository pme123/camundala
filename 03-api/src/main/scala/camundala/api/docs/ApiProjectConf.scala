package camundala.api
package docs

import com.typesafe.config.ConfigFactory

import scala.collection.Seq
import scala.jdk.CollectionConverters.CollectionHasAsScala

case class ApiProjectConf(
    org: String,
    name: String,
    version: String = ApiProjectConf.defaultVersion,
    dependencies: Seq[DependencyConf] = Seq.empty,
    changelog: Seq[String] = Seq.empty,
    isNew: Boolean = false
):
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"
end ApiProjectConf

object ApiProjectConf:
  lazy val defaultVersion = "0.1.0-SNAPSHOT"

  def apply(
             packageFile: os.Path
           ): ApiProjectConf =
    apply(packageFile, Seq.empty, false)
  def apply(
      packageFile: os.Path,
      changelog: Seq[String],
      isNew: Boolean
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
    ApiProjectConf(org, name, version, dependencies.toSeq, changelog, isNew)
  end apply
  def init(projectName: String) =
    ApiProjectConf(
      org = projectName.split("-").head,
      name = projectName
    )
end ApiProjectConf

case class DependencyConf(
    org: String,
    name: String,
    version: String
):
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"

  def equalTo(packageConf: ApiProjectConf): Boolean =
    packageConf.org == org && packageConf.name == name && packageConf.minorVersion == minorVersion
end DependencyConf

object DependencyConf:
  def apply(dependency: String): DependencyConf =
    val dArray = dependency.replace("\"", "").split(":")
    DependencyConf(dArray(0), dArray(1), dArray(2))

end DependencyConf
