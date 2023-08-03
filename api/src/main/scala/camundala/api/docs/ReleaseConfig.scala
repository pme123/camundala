package camundala.api.docs

import com.typesafe.config.ConfigFactory


case class ReleaseConfig(
    releaseTag: String,
    jiraReleaseUrl: Option[String],
    releaseNotes: String,
    released: Boolean,
) :
  lazy val releasedLabel: String =
    s"""${if (released) "\n" else "> **Preview to the next Release**\n\n"}
       |_Updated for Release $${release.tag} - created on $${created.day}_
       |""".stripMargin

object ReleaseConfig :
  lazy val releaseConfig = 
    val configFile = (os.pwd / "CONFIG.conf")
    val config = ConfigFactory.parseFile(configFile.toIO)
    ReleaseConfig(
      config.getString("release.tag"),
      if (config.hasPath("jira.release.url")) Some(config.getString("jira.release.url")) else None,
      config.getString("release.notes"),
      config.getBoolean("released"),
    )
  
end ReleaseConfig      




