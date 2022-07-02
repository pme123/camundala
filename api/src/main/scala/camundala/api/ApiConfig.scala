package camundala
package api

import bpmn.*
import sttp.tapir.openapi.Contact

case class ApiConfig(
    // define tenant if you have one
    tenantId: Option[String] = None,
    contact: Option[Contact] = None,
    // REST endpoint
    endpoint: String = "http://localhost:8080/engine-rest",
    basePath: Path = pwd,
    cawemoFolder: Option[String] = None,
    openApiPath: Path = pwd / "openApi.yml",
    postmanOpenApiPath: Path = pwd / "postmanOpenApi.yml",
    openApiDocuPath: Path = pwd / "OpenApi.html",
    postmanOpenApiDocuPath: Path = pwd / "PostmanOpenApi.html",
    jiraUrls: Map[String, String] = Map.empty,
    localProjectPaths: Seq[Path] = Seq(os.pwd / os.up),
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

  def withPort(port: Int): ApiConfig =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withDocProjectUrl(url: String => String): ApiConfig =
    copy(docProjectUrl = url)

  def withLocalProjectPaths(paths: Path*): ApiConfig =
    copy(localProjectPaths = paths)

  def withJiraUrls(urls: (String, String)*): ApiConfig =
    copy(jiraUrls = urls.toMap)

