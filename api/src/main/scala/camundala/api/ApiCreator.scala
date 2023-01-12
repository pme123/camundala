package camundala.api

import io.circe.*
import io.circe.syntax.*
import os.*
import sttp.apispec.openapi.*
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.json.circe.*
import sttp.tapir.{EndpointInput, PublicEndpoint}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.matching.Regex

trait ApiCreator
    extends ApiDsl,
      PostmanApiCreator,
      TapirApiCreator,
      App:

  def document(apis: CApi*): Unit =
    toOpenApi(ApiDoc(apis.toList))

  protected def toOpenApi(apiDoc: ApiDoc): Unit =
    writeOpenApi(
      apiConfig.openApiPath,
      openApi(apiDoc),
      apiConfig.openApiDocuPath
    )
    writeOpenApi(
      apiConfig.postmanOpenApiPath,
      postmanOpenApi(apiDoc),
      apiConfig.postmanOpenApiDocuPath
    )
    println(s"Check Open API Docu: ${apiConfig.openApiDocuPath}")

  protected lazy val openAPIDocsInterpreter =
    OpenAPIDocsInterpreter(docsOptions =
      OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
    )

  protected def openApi(apiDoc: ApiDoc): OpenAPI =
    val endpoints = create(apiDoc)
    openAPIDocsInterpreter
      .toOpenAPI(endpoints, info(title, description))

  protected def postmanOpenApi(apiDoc: ApiDoc): OpenAPI =
    val endpoints = createPostman(apiDoc)
    openAPIDocsInterpreter
      .toOpenAPI(endpoints, info(title, postmanDescription))
      .servers(servers)

  protected def createChangeLog(): String =
    val changeLogFile = basePath / "CHANGELOG.md"
    if (changeLogFile.toIO.exists())
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
  protected def postmanDescription: Option[String] =
    description.map(descr => s"""
                                |**This is for Postman - to have example requests. Be aware the Output is not provided!**
                                |
                                |$descr
                                |""".stripMargin)

  private def writeOpenApi(path: Path, api: OpenAPI, docPath: Path): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")
    println(s"See Open API Html $docPath")

end ApiCreator
