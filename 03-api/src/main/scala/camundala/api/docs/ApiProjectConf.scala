package camundala.api
package docs

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.CollectionHasAsScala

case class ApiProjectConf(
    org: String,
    name: String,
    version: String,
    dependencies: Seq[DependencyConf],
    changelog: Seq[String],
    isNew: Boolean
) {
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"
}

object ApiProjectConf:

  def apply(
      packageFile: os.Path,
      changelog: Seq[String] = Seq.empty,
      isNew: Boolean = false
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

end ApiProjectConf

case class DependencyConf(
    org: String,
    name: String,
    version: String,
) :
  lazy val minorVersion: String = version.split("\\.").take(2).mkString(".")
  lazy val fullName = s"$org:$name:$version"

  def equalTo(packageConf: ApiProjectConf): Boolean =
    packageConf.org == org && packageConf.name == name && packageConf.minorVersion == minorVersion

object DependencyConf :
  def apply(dependency: String): DependencyConf = {
    val dArray = dependency.replace("\"", "").split(":")
    DependencyConf(dArray(0), dArray(1), dArray(2))
  }

end DependencyConf