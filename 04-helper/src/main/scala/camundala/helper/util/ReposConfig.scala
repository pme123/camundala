package camundala.helper.util

case class ReposConfig(
    credentials: Seq[RepoCredentials] = Seq.empty,
    // first repo is the release Repo!
    repos: Seq[RepoConfig] = Seq.empty,
):

  def sbtCredentials: String =
    credentials.map(c => s"${c.name}Credentials").mkString(", ")

  def sbtRepos: String =
    repos.map(r => s"${r.name}Repo").mkString(", ")

end ReposConfig
object ReposConfig:
  lazy val dummyRepos = ReposConfig(
    repos = Seq(RepoConfig.Gitlab(
      "release",
      "???"
    ))
  )
end ReposConfig

sealed trait RepoConfig:
  def name: String
  def sbtContent: String
  def ammoniteRepo: String
end RepoConfig

object RepoConfig:
  case class Gitlab(
      name: String,
      repo: String,
      descr: String = "",
      realm: String = "gitlab"
  ) extends RepoConfig:
    lazy val sbtContent =
      s"""  // $descr
         |  lazy val ${name}RepoStr = 
         |    "$repo"
         |  lazy val ${name}Repo: MavenRepository = "$realm" at ${name}RepoStr
         |""".stripMargin
    lazy val ammoniteRepo = "/*NOT SUPPORTED*/"
  end Gitlab

  case class Artifactory(
      name: String,
      artifactoryApiUrl: String,
      repo: String,
      usernameEnv: String,
      passwordEnv: String,
      descr: String = "",
      realm: String = "Artifactory Realm"
  ) extends RepoConfig:

    lazy val sbtContent =
      s""" // $descr
         |  lazy val ${name}RepoStr = "$artifactoryApiUrl/$repo"
         |  lazy val ${name}Repo: MavenRepository = "$realm" at ${name}RepoStr
         |""".stripMargin

    lazy val ammoniteRepo: String =
      s"""  MavenRepository
         |    .of("$artifactoryApiUrl/$repo")
         |    .withCredentials(Credentials.of(sys.env("$usernameEnv"),
         |      sys.env("$passwordEnv")))""".stripMargin

    lazy val repoSearch: (String, String) => String =
      val username = sys.env(usernameEnv)
      val password = sys.env(passwordEnv)

      (project: String, org: String) =>
        val result = os.proc(
          "curl",
          s"$artifactoryApiUrl/api/search/latestVersion?g=$org&a=$project&repos=$repo",
          "-u",
          s"$username:$password"
        ).call()
        result.out.text()
    end repoSearch
  end Artifactory

end RepoConfig

sealed trait RepoCredentials:
  def name: String
  def sbtContent: String

object RepoCredentials:
  case class PrivateToken(
      name: String,
      repoHost: String,
      tokenEnv: String,
      realm: String = "GitLab Packages Registry"
  ) extends RepoCredentials:
    lazy val sbtContent: String =
      s"""  lazy val tokenName = sys.env.get("CI_JOB_TOKEN").map(_ => "Job-Token").getOrElse("Private-Token")
         |  lazy val ${name}Credentials: Credentials = (for {
         |    value <- sys.env.get("CI_JOB_TOKEN").orElse(sys.env.get("$tokenEnv"))
         |  } yield Credentials("$realm", "$repoHost", tokenName, value))
         |    .getOrElse(
         |      throw new IllegalArgumentException(
         |        "System Environment Variable $tokenEnv is not set."
         |      )
         |    )
         |""".stripMargin
  end PrivateToken

  case class UserPassword(
      name: String,
      repoHost: String,
      usernameEnv: String,
      passwordEnv: String,
      realm: String = "Artifactory Realm"
  ) extends RepoCredentials:
    lazy val sbtContent: String =
      s"""  lazy val ${name}Credentials: Credentials = (for {
         |    user <- sys.env.get("$usernameEnv")
         |    pwd <- sys.env.get("$passwordEnv")
         |  } yield Credentials("$realm", "$repoHost", user, pwd))
         |    .getOrElse(throw new IllegalArgumentException(
         |      "System Environment Variables $usernameEnv and/ or $passwordEnv are not set."
         |    ))
         |""".stripMargin
  end UserPassword
end RepoCredentials
