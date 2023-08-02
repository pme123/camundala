package camundala.myCompany

import camundala.api.*

lazy val postmanApiConfig = ApiConfig()
  .withEndpoint("http://localhost:9000/public/api")

lazy val myCompanyConfig = postmanApiConfig
  .withBasePath(os.pwd / "examples" / "myCompany")
  .withDocProjectUrl(project => s"https://webstor.ch/$project")
  .withGitConfigs(myCompanyGitConfigs)
  .withDiagramDownloadPath("diagrams")

private lazy val gitDir = os.pwd / os.up / os.up / "git-temp" / "camundala"

private lazy val myCompanyGitConfigs =
  GitConfigs(
    gitDir = gitDir,
    gitConfigs = Seq(
      GitConfig(
        "https://github.com/pme123/camundala.git",
        myProjects,
        groupedProjects = true
      )
    )
  )

private lazy val myProjects: Seq[ProjectConfig] = Seq(
  ProjectConfig(
    name = "exampleDemos",
    path = _ => os.rel / "examples" / "demos",
    group = demos,
    color = "#f4ffcc"
  ),
  ProjectConfig(
    "exampleInvoiceC7Version",
    _ => os.rel / "examples" / "invoice" / "camunda7",
    group = invoices,
    color = "#c8feda"
  ),
  ProjectConfig(
    "exampleTwitterC8Version",
    _ => os.rel / "examples" / "twitter" / "camunda8",
    group = twitter,
    color = "#f2d9d9"
  )
)

private lazy val demos = ProjectGroup("demos", "purple")
private lazy val invoices = ProjectGroup("demos", "green")
private lazy val twitter = ProjectGroup("demos", "green")


