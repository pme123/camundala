package camundala
package api

import os.Path
import sttp.apispec.openapi.Contact

case class ApiConfig(
    companyId: String,
    // define tenant if you have one
    tenantId: Option[String] = None,
    // contact email / phone, if there are questions
    contact: Option[Contact] = None,
    // REST endpoint (for testing API)
    endpoint: String = "http://localhost:8080/engine-rest",
    // Base Path of your project (if changed - all doc paths will be adjusted)
    basePath: os.Path = os.pwd,
    // If you work with JIRA, you can add matchers that will create automatically URLs to JIRA Tasks
    jiraUrls: Map[String, String] = Map.empty,
    // Configure your project setup
    projectsConfig: ProjectsConfig = ProjectsConfig(),
    // Configure your template generation
    modelerTemplateConfig: ModelerTemplateConfig = ModelerTemplateConfig(),
    // The URL of your published documentations
    // myProject => s"http://myCompany/bpmnDocs/${myProject}"
    docProjectUrl: String => String = proj => s"No URL defined for $proj",
    // If you want to integrate your BPMNs and DMNs in your Documentation.
    // Add the path the diagrams are located on your webserver.
    // myProject => s"http://myCompany/bpmnDocs/${myProject}/${diagramDownloadPath}"
    // if you want to have a diagram - you must define this!
    diagramDownloadPath: Option[String] = None,
    // if you want to adjust the diagramName
    diagramNameAdjuster: Option[String => String] = None,
    // function to extract project and the reference id from a reference (CallActivity, Dmn or ExternalWorker)
    // default returns the first part of the reference as project (e.g. mycompany from mycompany-product)
    projectRefId: String => (String, String) =
      pr => pr.split("-").head -> pr,
):
  val catalogPath: os.Path = basePath / catalogFileName

  lazy val openApiPath: os.Path            = basePath / ApiConfig.openApiPath
  lazy val postmanOpenApiPath: os.Path     = basePath / ApiConfig.postmanOpenApiPath
  lazy val openApiDocuPath: os.Path        = basePath / ApiConfig.openApiHtmlPath
  lazy val postmanOpenApiDocuPath: os.Path = basePath / ApiConfig.postmanOpenApiHtmlPath

  lazy val projectGroups: Seq[ProjectGroup] = projectsConfig.projectConfigs
    .map(_.group)
    .distinct

  def withTenantId(tenantId: String): ApiConfig =
    copy(tenantId = Some(tenantId))

  def withBasePath(path: os.Path): ApiConfig =
    copy(
      basePath = path
    )

  def withEndpoint(ep: String): ApiConfig =
    copy(endpoint = ep)

  def withPort(port: Int): ApiConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withDocProjectUrl(url: String => String): ApiConfig =
    copy(docProjectUrl = url)

  def withDiagramDownloadPath(diagramDownloadPath: String): ApiConfig =
    copy(diagramDownloadPath = Some(diagramDownloadPath))

  def withProjectsConfig(gitConfigs: ProjectsConfig): ApiConfig =
    copy(projectsConfig = gitConfigs)

  def withModelerTemplateConfig(modelerTemplateConfig: ModelerTemplateConfig): ApiConfig =
    copy(modelerTemplateConfig = modelerTemplateConfig)

  def addGitConfig(gitConfig: GroupedProjectConfig): ApiConfig =
    copy(projectsConfig =
      projectsConfig.copy(groupedConfigs = projectsConfig.groupedConfigs :+ gitConfig)
    )

  def withJiraUrls(urls: (String, String)*): ApiConfig =
    copy(jiraUrls = urls.toMap)

  def addJiraUrl(jiraTag: String, url: String): ApiConfig =
    copy(jiraUrls = jiraUrls + (jiraTag -> url))

  def withDiagramNameAdjuster(adjuster: String => String): ApiConfig =
    copy(diagramNameAdjuster = Some(adjuster))

  def withProjectRefId(projectRefId: String => (String, String)): ApiConfig =
    copy(projectRefId = projectRefId)

  def refIdentShort(refIdent: String): String =
    projectsConfig.refIdentShort(refIdent, companyId)

  def refIdentShort(refIdent: String, projectName: String): String =
    projectsConfig.refIdentShort(refIdent, companyId, projectName)

  lazy val projectConfPath: Path = basePath / projectsConfig.projectConfPath
end ApiConfig
object ApiConfig:
  lazy val openApiPath: os.RelPath = os.rel / "03-api" / "OpenApi.yml"
  lazy val postmanOpenApiPath: os.RelPath     = os.rel / "03-api" / "PostmanOpenApi.yml"
  lazy val openApiHtmlPath: os.RelPath        = os.rel / "03-api" / "OpenApi.html"
  lazy val postmanOpenApiHtmlPath: os.RelPath = os.rel / "03-api" / "PostmanOpenApi.html"

case class ProjectsConfig(
    // Path, where the Git Projects are cloned - for dependency check.
    // this for the structure: dev-myCompany/projects/myProject
    gitDir: os.Path = os.pwd / os.up / os.up / "git-temp",
    // Path to your ApiProjectConf
    projectConfPath: os.RelPath = defaultProjectPath,
    groupedConfigs: Seq[GroupedProjectConfig] = Seq.empty
):
  lazy val isConfigured: Boolean = groupedConfigs.nonEmpty

  def withProjectConfPath(path: os.RelPath): ProjectsConfig =
    copy(projectConfPath = path)

  def projectCloneUrl(projectName: String): Option[String] =
    groupedConfigs
      .find(_.containsProject(projectName))
      .map(_.cloneUrl)

  lazy val init: Unit =
    groupedConfigs.foreach(_.init(gitDir))
  end init

  def initProject(projectName: String): Unit =
    groupedConfigs.foreach(_.initProject(gitDir, projectName))
  end initProject

  def projectConfig(projectName: String): Option[ProjectConfig] =
    projectConfigs.find(_.name == projectName)

  lazy val projectConfigs: Seq[ProjectConfig] = groupedConfigs.flatMap(_.projects)

  lazy val colors: Seq[(String, String)] = projectConfigs.map { project =>
    project.name -> project.color
  }

  def hasProjectGroup(
      projectName: String,
      projectGroup: ProjectGroup
  ): Boolean =
    groupedConfigs
      .flatMap(_.projects)
      .find(_.name == projectName)
      .exists(_.group == projectGroup)

  def refIdentShort(refIdent: String, companyId: String, projectName: String): String =
    refIdent
      .replace(s"$companyId-", "") // mycompany-myproject-myprocess -> myproject-myprocess
      .replace(
        s"${projectName.replace(s"$companyId-", "")}-",
        ""
      ) // myproject-myprocess -> myprocess
  end refIdentShort

  // if projectName is not known
  def refIdentShort(refIdent: String, companyId: String): String =
    val projectNames = projectConfigs.map(pc => pc.name)

    projectNames.find(refIdent.startsWith)
      .map(pn =>
        refIdent.replace(s"$pn-", "")  // case myCompany-myProject-myProcess
          .replace(s"$companyId-", "") // case myCompany-myProject > where no myProcess
      )
      .orElse(          // case myProject-myProcess
        projectNames.map(_.replace(s"$companyId-", ""))
          .find(refIdent.startsWith)
          .map(pn =>
            refIdent.replace(s"$pn-", "")
          )).getOrElse( // or any other process
        refIdent)
  end refIdentShort

end ProjectsConfig

case class GroupedProjectConfig(
    cloneUrl: String,
    projects: Seq[ProjectConfig],
    groupedProjects: Boolean = false
):

  def init(gitDir: os.Path): Unit =
    if groupedProjects then
      updateProject(gitDir, cloneUrl)
    else
      projects.foreach { project =>
        val gitRepo = s"$cloneUrl/${project.name}.git"
        updateProject(project.absGitPath(gitDir), gitRepo)
      }

  def initProject(gitDir: os.Path, projectName: String): Unit =
    projects.find(_.name == projectName)
      .foreach { project =>
        val gitRepo = s"$cloneUrl/${project.name}.git"
        updateProject(project.absGitPath(gitDir), gitRepo)
      }
  end initProject

  def containsProject(projectName: String): Boolean =
    projects.exists(_.name == projectName)

  private def updateProject(gitProjectDir: os.Path, gitRepo: String): Unit =
    println(s"Git Project Dir: $gitProjectDir")
    println(s"Git Repo: $gitRepo")
    os.makeDir.all(gitProjectDir)
    if !(gitProjectDir / ".gitignore").toIO.exists() then
      os.proc("git", "clone", gitRepo, gitProjectDir)
        .callOnConsole(gitProjectDir)
    else
      os
        .proc("git", "checkout", "develop")
        .callOnConsole(gitProjectDir)
      os.proc("git", "pull", "origin", "develop")
        .callOnConsole(gitProjectDir)
    end if
  end updateProject
end GroupedProjectConfig

case class ProjectConfig(
    name: String,
    // path of project (name => gitProjectDir/${os.RelPath})
    path: String => os.RelPath = name => os.rel / name,
    // path where the BPMNs are - must be relative to the project path
    bpmnPath: os.RelPath = os.rel / "src" / "main" / "resources",
    group: ProjectGroup,
    color: String = "#fff"
):
  def absGitPath(gitDir: os.Path): os.Path  = gitDir / name
  def absBpmnPath(gitDir: os.Path): os.Path = absGitPath(gitDir) / bpmnPath
end ProjectConfig

case class ProjectGroup(
    name: String,
    color: String = "purple",
    fill: String = "#ddd"
)

case class ModelerTemplateConfig(
    schemaVersion: String = "0.16.0",
    templateRelativePath: os.RelPath = os.rel / ".camunda" / "element-templates",
    generateGeneralVariables: Boolean = true
):
  lazy val templatePath: Path = os.pwd / templateRelativePath

  lazy val schema =
    s"https://unpkg.com/@camunda/element-templates-json-schema@$schemaVersion/resources/schema.json"
end ModelerTemplateConfig
