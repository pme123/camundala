package camundala.myCompany

import camundala.api.*

lazy val postmanApiConfig = ApiConfig("myCompany")
  .withEndpoint("http://localhost:9000/public/api")

lazy val myCompanyConfig = postmanApiConfig
  .withBasePath(os.pwd / "05-examples" / "myCompany")
  .withDocBaseUrl("https://webstor.ch")
  .withProjectsConfig(myCompanyGitConfigs)


private lazy val myCompanyGitConfigs =
  ProjectsConfig(
    perGitRepoConfigs = Seq(
      ProjectsPerGitRepoConfig(
        "https://github.com/pme123/camundala.git",
        myProjects
      )
    )
  )

private lazy val myProjects: Seq[ProjectConfig] = Seq(
  ProjectConfig(
    name = "exampleDemos",
    group = demos,
    color = "#f4ffcc"
  ),
  ProjectConfig(
    "exampleInvoiceC7Version",
    group = invoices,
    color = "#c8feda"
  ),
  ProjectConfig(
    "exampleTwitterC8Version",
    group = twitter,
    color = "#f2d9d9"
  )
)

private lazy val demos = ProjectGroup("demos", "purple")
private lazy val invoices = ProjectGroup("demos", "green")
private lazy val twitter = ProjectGroup("demos", "green")
