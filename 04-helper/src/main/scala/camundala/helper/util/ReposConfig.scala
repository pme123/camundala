package camundala.helper.util

case class ReposConfig(
    credentials: Seq[RepoCredentials] = Seq.empty,
    // first repo is the release Repo!
    repos: Seq[RepoConfig] = Seq.empty,
    repoSearch: (project: String, org: String) => String = VersionHelper.repoSearchMavenCentral,
    ammoniteRepos: Seq[String] = Seq.empty
):

  def sbtCredentials: String =
    credentials.map(c => s"${c.name}Credentials").mkString(", ")

  def sbtRepos: String =
    repos.map(r => s"${r.name}Repo").mkString(", ")

end ReposConfig
object ReposConfig:
  lazy val dummyRepos = ReposConfig(
    repos = Seq(RepoConfig(
      "release",
      "???"
    ))
  )
case class RepoConfig(
    name: String,
    sbtContent: String
)

case class RepoCredentials(
    name: String,
    sbtContent: String
)
