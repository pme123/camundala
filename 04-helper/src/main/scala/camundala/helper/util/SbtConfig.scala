package camundala.helper.util

case class SbtConfig(
    // sbt settings for publishing
    reposConfig: ReposConfig = ReposConfig.dummyRepos,
    // sbt settings for docker
    dockerSettings: Option[String] = None
)
