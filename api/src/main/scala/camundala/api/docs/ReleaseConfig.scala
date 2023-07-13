package camundala.api.docs

import java.awt.Color
import camundala.api.ProjectGroup

case class ReleaseConfig(
    releaseTag: String,
    jiraReleaseUrl: Option[String],
    releaseNotes: String,
    released: Boolean,
    projectGroups: Seq[ProjectGroup],
) {
  lazy val releasedLabel: String =
    s"""${if (released) "\n" else "> **Preview to the next Release**\n\n"}
       |_Updated for Release $${release.tag} - created on $${created.day}_
       |""".stripMargin
}
/*
object ReleaseConfig:
  def releaseConfig(basePath: os.Path): ReleaseConfig =
    val configFile = (basePath / "CONFIG.conf")
    println(s"Config File: $configFile")
    val config = ConfigFactory.parseFile(configFile.toIO)
    val releaseConfig = ReleaseConfig(
      config.getString("release.tag"),
      if (config.hasPath("jira.release.url"))
        Some(config.getString("jira.release.url"))
      else None,
      config.getString("release.notes"),
      config.getBoolean("released")
    )
    println(s"Release Config: $releaseConfig")
    releaseConfig
  end releaseConfig

end ReleaseConfig
*/


