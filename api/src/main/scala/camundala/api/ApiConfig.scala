package camundala
package api

import camundala.bpmn.*
import os.Path
import sttp.apispec.openapi.Contact

case class ApiConfig(
    // define tenant if you have one
    tenantId: Option[String] = None,
    // contact email / phone, if there are questions
    contact: Option[Contact] = None,
    // REST endpoint (for testing API)
    endpoint: String = "http://localhost:8080/engine-rest",
    // Base Path of your project (if changed - all doc paths will be adjusted)
    basePath: Path = pwd,
    // If your project is on cawemo, add here the Id of the folder of your bpmns.
    cawemoFolder: Option[String] = None,
    openApiPath: Path = pwd / "openApi.yml",
    postmanOpenApiPath: Path = pwd / "postmanOpenApi.yml",
    openApiDocuPath: Path = pwd / "OpenApi.html",
    postmanOpenApiDocuPath: Path = pwd / "PostmanOpenApi.html",
    // If you work with JIRA, you can add matchers that will create automatically URLs to JIRA Tasks
    jiraUrls: Map[String, String] = Map.empty,
    // Git Projects: Configure, projects you want to evaluate for dependency resolution
    gitConfigs: GitConfigs = GitConfigs(os.pwd / os.up),
    // The URL of your published documentations
    // myProject => s"http://myCompany/bpmnDocs/${myProject}"
    docProjectUrl: String => String = proj => s"No URL defined for $proj",
    // If you want to integrate your BPMNs and DMNs in your Documentation.
    // Add the path the diagrams are located on your webserver.
    // myProject => s"http://myCompany/bpmnDocs/${myProject}/${diagramDownloadPath}"
    // if you want to have a diagram - you must define this!
    diagramDownloadPath: Option[String] = None
):
  val catalogPath: Path = basePath / catalogFileName

  def withTenantId(tenantId: String): ApiConfig =
    copy(tenantId = Some(tenantId))

  def withCawemoFolder(folderName: String): ApiConfig =
    copy(cawemoFolder = Some(folderName))

  def withBasePath(path: Path): ApiConfig =
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

case class GitConfigs(
    // Path, where the Git Projects are cloned.
    gitDir: os.Path = os.pwd / os.up / "git-temp",
    gitConfigs: Seq[GitConfig] = Seq.empty,
    bpmnPath: os.RelPath = os.rel / "src" / "main" / "resources"
):
  lazy val isConfigured: Boolean = gitConfigs.nonEmpty

  lazy val init: Seq[(String, Path)] =
    gitConfigs.flatMap(_.init(gitDir, bpmnPath))
  end init

end GitConfigs

case class GitConfig(
    cloneUrl: String,
    projects: Seq[String]
):

  def init(gitDir: os.Path, bpmnPath: os.RelPath): Seq[(String, Path)] =
    projects.map(initProject(_, gitDir, bpmnPath))
  end init

  private def initProject(
      project: String,
      gitDir: os.Path,
      bpmnPath: os.RelPath
  ): (String, os.Path) =
    val gitProjectDir = gitDir / project
    os.makeDir.all(gitProjectDir)
    if (!(gitProjectDir / ".gitignore").toIO.exists()) {
      os.proc("git", "clone", s"$cloneUrl/$project.git", gitProjectDir)
        .callOnConsole(gitProjectDir)
    } else {
      os.proc("git", "pull").callOnConsole(gitProjectDir)
    }
    project -> gitProjectDir / bpmnPath
  end initProject
