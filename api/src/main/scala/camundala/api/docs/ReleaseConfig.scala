package camundala.api.docs

import java.awt.Color
import camundala.api.ProjectGroup
import com.typesafe.config.ConfigFactory

case class ReleaseConfig(
    releaseTag: String,
    jiraReleaseUrl: Option[String],
    releaseNotes: String,
    released: Boolean,
) {
  lazy val releasedLabel: String =
    s"""${if (released) "\n" else "> **Preview to the next Release**\n\n"}
       |_Updated for Release $${release.tag} - created on $${created.day}_
       |""".stripMargin
}




