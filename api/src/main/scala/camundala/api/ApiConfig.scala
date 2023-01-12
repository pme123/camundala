package camundala
package api

import bpmn.*
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
    // Path where you have all your projects (used to search for dependencies)
    localProjectPaths: Seq[Path] = Seq(os.pwd / os.up),
    // The URL of your published documentations
    // myProject => s"http://myCompany/bpmnDocs/${myProject}"
    docProjectUrl: String => String = proj => s"No URL defined for $proj"
):

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
      postmanOpenApiDocuPath = path / "PostmanOpenApi.html",
    )

  def withEndpoint(ep: String): ApiConfig =
    copy(endpoint = ep)

  def withPort(port: Int): ApiConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withDocProjectUrl(url: String => String): ApiConfig =
    copy(docProjectUrl = url)

  def withLocalProjectPaths(paths: Path*): ApiConfig =
    copy(localProjectPaths = paths)

  def withJiraUrls(urls: (String, String)*): ApiConfig =
    copy(jiraUrls = urls.toMap)

