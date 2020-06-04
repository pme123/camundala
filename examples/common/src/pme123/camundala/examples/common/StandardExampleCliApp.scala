package pme123.camundala.examples.common

import pme123.camundala
import pme123.camundala.cli.{ProjectInfo, StandardCliApp}

trait StandardExampleCliApp extends StandardCliApp {

  lazy val projectInfo: ProjectInfo =
    ProjectInfo(
      title,
      camundala.BuildInfo.organization,
      camundala.BuildInfo.version,
      s"${camundala.BuildInfo.url}/tree/master/examples/$ident",
      camundala.BuildInfo.license
    )

}
