package camundala.myCompany

import camundala.api.*

lazy val postmanApiConfig = ApiConfig()
  .withEndpoint("http://localhost:9000/public/api")

lazy val myCompanyConfig = postmanApiConfig
  .withBasePath(os.pwd / "05-examples" / "myCompany")
  .withDocProjectUrl(project => s"https://webstor.ch/$project")
  .withProjectsConfig(myCompanyGitConfigs)
  .withDiagramDownloadPath("diagrams")

private lazy val gitDir = os.pwd / os.up / os.up / "git-temp" / "camundala"

private lazy val myCompanyGitConfigs =
  ProjectsConfig(
    gitDir = gitDir,
    groupedConfigs = Seq(
      GroupedProjectConfig(
        "https://github.com/pme123/camundala.git",
        myProjects,
        groupedProjects = true
      )
    )
  )

private lazy val myProjects: Seq[ProjectConfig] = Seq(
  ProjectConfig(
    name = "exampleDemos",
    path = _ => os.rel / "05-examples" / "demos",
    group = demos,
    color = "#f4ffcc"
  ),
  ProjectConfig(
    "exampleInvoiceC7Version",
    _ => os.rel / "05-examples" / "invoice" / "camunda7",
    group = invoices,
    color = "#c8feda"
  ),
  ProjectConfig(
    "exampleTwitterC8Version",
    _ => os.rel / "05-examples" / "twitter" / "camunda8",
    group = twitter,
    color = "#f2d9d9"
  )
)

private lazy val demos = ProjectGroup("demos", "purple")
private lazy val invoices = ProjectGroup("demos", "green")
private lazy val twitter = ProjectGroup("demos", "green")


