package camundala.api

import camundala.domain.MockedServiceResponse
import io.circe.syntax.*
import sttp.apispec.openapi.*
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.matching.Regex

trait ApiCreator extends PostmanApiCreator, TapirApiCreator, App:

  def document(apis: CApi*): Unit =
    val apiDoc = ApiDoc(apis.toList)
    writeOpenApis(apiDoc)
    writeCatalog(apiDoc)

  private def writeOpenApis(apiDoc: ApiDoc): Unit =
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
         |<details>
         |<summary>**CHANGELOG.md**</summary>
         |<p>
         |
         |${os.read
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
  end createChangeLog

  protected def createGeneralVariables(): String =
    s"""|<p/>
        |<details>
        |<summary>
        |<b><i>General Variables</i></b>
        |</summary>
        |
        |<p>
        |
        |### Processes and ServiceProcesses
        |
        |**outputMock**:
        |
        |Mock this Service (`Out`) with:
        |
        |- DSL:
        |```scala
        |process(..) // or serviceProcess(..)
        |  .mockWith(outputMock)
        |```
        |
        |- Json (you find examples in each process):
        |```json
        |...
        |"outputMock": {..},
        |...
        |```
        |
        |**servicesMocked**:
        |
        |Mock the ServiceProcesses (Workers only) with their default Mock:
        |
        |- DSL:
        |```scala
        |process(..) // or serviceProcess(..)
        |  .mockServices
        |```
        |
        |- Json:
        |```json
        |...
        |"servicesMocked": true,
        |...
        |```
        |
        |**impersonateUserId**:
        |
        |User-ID of a User that should be taken to authenticate to the services.
        |This must be supported by your implementation. *Be caution: this may be a security issue!*.
        |It is helpful if you have Tokens that expire, but long running Processes.
        |
        |- DSL:
        |```scala
        |process(..) // or serviceProcess(..)
        |  .withImpersonateUserId(impersonateUserId)
        |```
        |
        |- Json:
        |```json
        |...
        |"impersonateUserId": "myUserName",
        |...
        |```
        |
        |### ServiceProcesses
        |**outputServiceMock**:
        |
        |Mock the Inner-Service (`MockedServiceResponse[ServiceOut]`).
        |
        |- DSL:
        |```scala
        |serviceProcess(..)
        |  .mockServiceWith(MockedServiceResponse
        |     .success200(inOut.defaultServiceMock))
        |```
        |
        |- Json (you find examples in each process):
        |```json
        |...
        |"outputServiceMock": ${MockedServiceResponse
      .success200("Example String Body")
      .asJson},
        |...
        |```
        |
        |**handledErrors**:
        |
        |A list of error codes that are handled (`BpmnError`)
        |
        |- DSL:
        |```scala
        |serviceProcess(..)
        |  .handleError("400")
        |  .handleError("404")
        |```
        |
        |- Json
        |```json
        |...
        |"handledErrors": Seq[String] = ["400", "404"],
        |...
        |```
        |
        |**regexHandledErrors**:
        |
        |Handling Errors with the messages that match a list of Regex expressions
        |
        |- DSL:
        |```scala
        |serviceProcess(..)
        |  .handleErrorWithRegex("SQL exception")
        |  .handleErrorWithRegex("\\"errorNr\\":\\"20000\\"")
        |```
        |
        |- Json:
        |```json
        |...
        |"regexHandledErrors": ["SQL exception", "\\"errorNr\\":\\"20000\\""]
        |...
        |```
        |
        |</p>
        |</details>
        |<p/>
        """.stripMargin
  end createGeneralVariables
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
      os.read.lines(readme).tail.mkString("\n")
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
       |
       |${createGeneralVariables()}
       |""".stripMargin
  )
  protected def postmanDescription: Option[String] =
    description.map(descr => s"""
                                |**This is for Postman - to have example requests. Be aware the Output is not provided!**
                                |
                                |$descr
                                |""".stripMargin)

  private def writeOpenApi(
      path: os.Path,
      api: OpenAPI,
      docPath: os.Path
  ): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")
    println(s"See Open API Html $docPath")

  private def writeCatalog(apiDoc: ApiDoc): Unit =
    val catalogPath = apiConfig.catalogPath
    if (os.exists(catalogPath))
      os.remove(catalogPath)
    val catalog = toCatalog(apiDoc)
    os.write(catalogPath, catalog)
    println(s"Created Catalog $catalogPath")

  private def toCatalog(apiDoc: ApiDoc): String =
    val optimizedApis =
      if (apiConfig.catalogOptimized)
        optimizeApis(apiDoc)
      else apiDoc.apis
    s"""### $title
       |${toCatalog(optimizedApis)}
       |""".stripMargin

  private def optimizeApis(apiDoc: ApiDoc): List[CApi] =
    apiDoc.apis.foldLeft(List.empty[CApi]) {
      case (result, groupedApi: GroupedApi) =>
        val filteredApis =
          groupedApi.apis.flatMap(api => checkApi(api, result).toList)
        if (filteredApis.nonEmpty)
          result :+ groupedApi.withApis(filteredApis)
        else result
      case (result, otherApi: CApi) =>
        result ++ checkApi(otherApi, result).toList
    }

  // check if the Api is already listed in the result apis
  private def checkApi(api: CApi, result: List[CApi]): Option[CApi] =
    if (
      result
        .flatMap {
          case api: GroupedApi => api.apis
          case other => Seq(api)
        }
        .exists(a => a.name == api.name)
    )
      None
    else
      Some(api)

  private def toCatalog(
      apis: List[CApi],
      groupAnchor: Option[String] = None
  ): String =
    apis
      .map {
        case pa @ ProcessApi(name, inOut, _, apis, _) =>
          if (groupAnchor.nonEmpty)
            s"- ${createLink(pa.endpointName, groupAnchor)}"
          else
            s"""
               |#### ${createLink(name)}
               |- ${createLink(pa.endpointName, Some(name))}
               |""".stripMargin + toCatalog(apis, Some(name))
        case api: GroupedApi =>
          s"\n#### ${createLink(api.name)}\n" + toCatalog(
            api.apis,
            Some(api.name)
          )
        case api: InOutApi[?, ?] =>
          s"- ${createLink(s"${api.typeName}: ${api.name}", groupAnchor)}"
      }
      .mkString("\n")
  end toCatalog

  private def createLink(
      name: String,
      groupAnchor: Option[String] = None
  ): String =
    val projName = apiConfig.docProjectUrl(projectName)
    val anchor = groupAnchor
      .map(a =>
        s"tag/${a.replace(" ", "-")}/operation/${name.replace(" ", "%20")}"
      )
      .getOrElse(s"tag/${name.replace(" ", "-")}")
    s"[$name]($projName/OpenApi.html#$anchor)"
  end createLink

end ApiCreator
