package camundala
package api

import camundala.bpmn.*
import sttp.apispec.openapi.Contact

case class ApiConfig(
    // define tenant if you have one
    tenantId: Option[String] = None,
    // contact email / phone, if there are questions
    contact: Option[Contact] = None,
    // REST endpoint (for testing API)
    endpoint: String = "http://localhost:8080/engine-rest",
    // Base Path of your project (if changed - all doc paths will be adjusted)
    basePath: os.Path = os.pwd,
    // If your project is on cawemo, add here the Id of the folder of your bpmns.
    cawemoFolder: Option[String] = None,
    openApiPath: os.Path = os.pwd / "openApi.yml",
    postmanOpenApiPath: os.Path = os.pwd / "postmanOpenApi.yml",
    openApiDocuPath: os.Path = os.pwd / "OpenApi.html",
    postmanOpenApiDocuPath: os.Path = os.pwd / "PostmanOpenApi.html",
    // If you work with JIRA, you can add matchers that will create automatically URLs to JIRA Tasks
    jiraUrls: Map[String, String] = Map.empty,
    // Git Projects: Configure, projects you want to evaluate for dependency resolution
    gitConfigs: GitConfigs = GitConfigs(),
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
    // by default the Api are optimized in a way that each Api is listed just ones.
    // so for example, if you list your DMNs extra, they will be removed from the catalog.md
    catalogOptimized: Boolean = true
):
  val catalogPath: os.Path = basePath / catalogFileName

  lazy val projectGroups = gitConfigs.projectConfigs
    .map(_.group)
    .distinct

  def withTenantId(tenantId: String): ApiConfig =
    copy(tenantId = Some(tenantId))

  def withCawemoFolder(folderName: String): ApiConfig =
    copy(cawemoFolder = Some(folderName))

  def withBasePath(path: os.Path): ApiConfig =
    copy(
      basePath = path,
      openApiPath = path / "openApi.yml",
      openApiDocuPath = path / "OpenApi.html",
      postmanOpenApiPath = path / "postmanOpenApi.yml",
      postmanOpenApiDocuPath = path / "PostmanOpenApi.html"
    )

  def withEndpoint(ep: String): ApiConfig =
    copy(endpoint = ep)

  def withPort(port: Int): ApiConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withDocProjectUrl(url: String => String): ApiConfig =
    copy(docProjectUrl = url)

  def withDiagramDownloadPath(diagramDownloadPath: String): ApiConfig =
    copy(diagramDownloadPath = Some(diagramDownloadPath))

  def withGitConfigs(gitConfigs: GitConfigs): ApiConfig =
    copy(gitConfigs = gitConfigs)

  def addGitConfig(gitConfig: GitConfig): ApiConfig =
    copy(gitConfigs =
      gitConfigs.copy(gitConfigs = gitConfigs.gitConfigs :+ gitConfig)
    )

  def withJiraUrls(urls: (String, String)*): ApiConfig =
    copy(jiraUrls = urls.toMap)

  def addJiraUrl(jiraTag: String, url: String): ApiConfig =
    copy(jiraUrls = jiraUrls + (jiraTag -> url))

  def withDiagramNameAdjuster(adjuster: String => String): ApiConfig =
    copy(diagramNameAdjuster = Some(adjuster))

  def withCatalogOptimization() =
    copy(catalogOptimized = true)

  def withoutCatalogOptimization() =
    copy(catalogOptimized = false)

case class GitConfigs(
    // Path, where the Git Projects are cloned.
    gitDir: os.Path = os.pwd / os.up / "git-temp",
    gitConfigs: Seq[GitConfig] = Seq.empty
):
  lazy val isConfigured: Boolean = gitConfigs.nonEmpty

  def projectCloneUrl(projectName: String): Option[String] =
    gitConfigs
      .find(_.containsProject(projectName))
      .map(_.cloneUrl)

  lazy val init: Unit =
    gitConfigs.foreach(_.init(gitDir))
  end init

  lazy val projectConfigs = gitConfigs.flatMap(_.projects)

  lazy val colors: Seq[(String, String)] = projectConfigs.map { project =>
    project.name -> project.color
  }

  def hasProjectGroup(
      projectName: String,
      projectGroup: ProjectGroup
  ): Boolean =
    gitConfigs
      .flatMap(_.projects)
      .find(_.name == projectName)
      .exists(_.group == projectGroup)

end GitConfigs

case class GitConfig(
    cloneUrl: String,
    projects: Seq[ProjectConfig],
    // from project String to path
    // default is s"$cString =>loneUrl/$project.git"
    groupedProjects: Boolean = false
):

  def init(gitDir: os.Path): Unit =
    if (groupedProjects)
      updateProject(gitDir, cloneUrl)
    else
      projects.map { project =>
        val gitRepo = s"$cloneUrl/${project.name}.git"
        updateProject(project.absGitPath(gitDir), gitRepo)
      }

  end init
  def containsProject(projectName: String): Boolean =
    projects.exists(_.name == projectName)

  private def updateProject(gitProjectDir: os.Path, gitRepo: String): Unit =
    println(s"Git Project Dir: $gitProjectDir")
    println(s"Git Repo: $gitRepo")
    os.makeDir.all(gitProjectDir)
    if (!(gitProjectDir / ".gitignore").toIO.exists()) {
      os.proc("git", "clone", gitRepo, gitProjectDir)
        .callOnConsole(gitProjectDir)
    } else {
      os
        .proc("git", "checkout", "develop")
        .callOnConsole(gitProjectDir)
      os.proc("git", "pull", "origin", "develop")
        .callOnConsole(gitProjectDir)
    }
  end updateProject
end GitConfig

case class ProjectConfig(
    name: String,
    // path of project (name => gitProjectDir/${os.RelPath})
    path: String => os.RelPath = name => os.rel / name,
    // path where the BPMNs are - must be relative to the project path
    bpmnPath: os.RelPath = os.rel / "src" / "main" / "resources",
    group: ProjectGroup,
    color: String = "#fff"
):
  def absGitPath(gitDir: os.Path): os.Path = gitDir / name
  def absBpmnPath(gitDir: os.Path): os.Path = absGitPath(gitDir) / bpmnPath
end ProjectConfig

case class ProjectGroup(
    name: String,
    color: String = "purple",
    fill: String = "#ddd"
)
