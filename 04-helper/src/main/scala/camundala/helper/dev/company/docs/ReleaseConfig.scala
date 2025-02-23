package camundala.helper.dev.company.docs

import com.typesafe.config.ConfigFactory

case class ReleaseConfig(
    releaseTag: String,
    jiraReleaseUrl: Option[String],
    releaseNotes: String,
    released: Boolean,
    releaseResponsible: Option[ResponsiblePerson]
):
  lazy val releasedLabel: String =
    s"""${
        if released then
          releaseResponsible.map(r => s"Approved by ${r.name} (${r.date})\n\n").getOrElse("\n")
        else "> **Preview to the next Release**\n\n"
      }
       |_Updated for Release $${release.tag} - created on $${created.day}_
       |""".stripMargin
end ReleaseConfig

object ReleaseConfig:
  def releaseConfig(basePath: os.Path) =
    val configFile = (basePath / "CONFIG.conf")
    val config     = ConfigFactory.parseFile(configFile.toIO)
    ReleaseConfig(
      config.getString("release.tag"),
      if config.hasPath("jira.release.url") then Some(config.getString("jira.release.url"))
      else None,
      config.getString("release.notes"),
      config.getBoolean("released"),
      if config.hasPath("release.responsible") then
        Some(
          ResponsiblePerson(
            config.getString("release.responsible.name"),
            config.getString("release.responsible.date")
          )
        )
      else None
    )
  end releaseConfig

end ReleaseConfig
