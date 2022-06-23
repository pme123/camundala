package camundala
package api

import api.ast.*
import bpmn.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.docs.openapi.*
import sttp.tapir.openapi.*
import sttp.tapir.openapi.circe.yaml.*
import io.circe.*
import io.circe.syntax.*
import sttp.tapir.json.circe.*

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.matching.Regex

trait TapirApiCreator extends App:

  protected def apiConfig: ApiConfig = ApiConfig()

  protected implicit def tenantId: Option[String] = apiConfig.tenantId

  protected def basePath: Path = apiConfig.basePath

  protected def title: String

  protected def version: String

  protected def servers = List(Server(apiConfig.endpoint))

  protected def info(title: String) =
    Info(title, version, description, contact = apiConfig.contact)

  protected def createChangeLog(): String =
    val changeLogFile = basePath / "CHANGELOG.md"
    if (changeLogFile.toIO.exists())
      //  createChangeLog(read(changeLogFile))
      s"""
         |# Changelog
         |
         |<details>
         |<summary>CHANGELOG.md</summary>
         |<p>
         |
         |${read
        .lines(changeLogFile)
        .tail
        .map(_.replace("##", "###"))
        .map(replaceJira(_, apiConfig.jiraUrls))
        .mkString("\n")}
         |
         |</p>
         |</details>
         |""".stripMargin
    else
      ""

  protected def replaceJira(
      line: String,
      jiraUrls: Map[String, String]
  ): String =
    jiraUrls.toList match
      case Nil => line
      case (k -> url) :: tail =>
        val regex = Regex(s"""$k-(\\d+)""")
        val matches = regex.findAllIn(line).toSeq
        val changed =
          matches.foldLeft(line)((a, b) => a.replace(b, s"[$b]($url/$b)"))
        replaceJira(changed, tail.toMap)

  protected def createReadme(): String =
    val readme = basePath / "README.md"
    if (readme.toIO.exists())
      read.lines(readme).tail.mkString("\n")
    else
      "There is no README.md in the Project."

  protected def description: Option[String] = Some(
    s"""
       |
       |Created at ${SimpleDateFormat().format(new Date())}
       |
       |**${//
    apiConfig.cawemoFolder
      .map(f => s"[Check Project on Cawemo](https://cawemo.com/folders/$f)")
      .mkString //
    }**
       |
       |${createReadme()}
       |
       |${createChangeLog()}
       |""".stripMargin
  )

  protected def run(apiDoc: ApiDoc): Unit =
    writeOpenApi(apiConfig.openApiPath, openApi(apiDoc))
    println(s"Check Open API Docu: ${apiConfig.openApiDocuPath}")

  protected lazy val openAPIDocsInterpreter =
    OpenAPIDocsInterpreter(docsOptions =
      OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
    )

  protected def openApi(apiDoc: ApiDoc): OpenAPI = {
    val endpoints = apiDoc.create()
    println(s"ENDPOINTS: ${endpoints.size}")
    openAPIDocsInterpreter
      .toOpenAPI(endpoints, info(title))
  }

  private def writeOpenApi(path: Path, api: OpenAPI): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")

  extension (apiDoc: ApiDoc)
    def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start API: $title - ${apiDoc.apis.size} top level APIs")
      apiDoc.apis.flatMap(_.create())
  end extension

  extension (groupedApi: GroupedApi)
    def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      val apis = groupedApi.apis.flatMap(_.create(groupedApi.name))
      groupedApi match
        case pa: ProcessApi[?, ?] =>
          pa.createEndpoint(pa.name) ++ apis
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def create(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case aa @ ActivityApi(name, inOut, _) =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          aa.createEndpoint(tag)
        case pa @ ProcessApi(name, _, _, apis) if apis.isEmpty =>
          println(s"ProcessApi: $tag - $name")
          pa.createEndpoint(tag)
        case ga: GroupedApi =>
          throw IllegalArgumentException(
            "Sorry, only one level of GroupedApis are allowed!"
          )

  end extension

  extension (inOutApi: InOutApi[?, ?])
    def createEndpoint(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val endpointType = inOutApi.inOut.getClass.getSimpleName
      val tagPath = tag.replace(" ", "")
      val path =
        if (tagPath == inOutApi.id)
          endpointType.toLowerCase() / inOutApi.id
        else
          endpointType.toLowerCase() / tagPath / inOutApi.id
      Seq(
        endpoint
          .name(s"$endpointType: ${inOutApi.name}")
          .tag(tag)
          .in(path)
          .summary(s"$endpointType: ${inOutApi.name}")
          .description(inOutApi.descr)
          .head
      ).map(ep => inOutApi.inMapper.map(ep.in).getOrElse(ep))
        .map(ep => inOutApi.outMapper.map(ep.out).getOrElse(ep))
  end extension

end TapirApiCreator
