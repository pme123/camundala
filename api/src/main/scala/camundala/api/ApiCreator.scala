package camundala.api

import camundala.bpmn.InputParams
import camundala.domain.MockedServiceResponse
import sttp.apispec.openapi.*
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.matching.Regex

trait ApiCreator extends PostmanApiCreator, TapirApiCreator, App:

  def supportedVariables: Seq[InputParams] =
    InputParams.values.toSeq

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
         |<summary><b><i>CHANGELOG.md</i></b></summary>
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
        |<b><i>Supported General Variables</i></b>
        |</summary>
        |
        |<p>
        |
        |### Processes
        |""".stripMargin +
      createGeneralVariable(
        InputParams.mockedSubprocesses,
        s"""Mock the SubProcesses with their default Mocks.
          |This is a list of the _SubProcesses processNames_ you want to mock.
          |${listOfStringsOrCommaSeparated("mySubProcess,myOtherSubProcess")}
          |""".stripMargin,
        """process(..)
          |  .mockSubProcesses("mySubProcess1", "mySubProcess2") // creates a list with SubProcessess
          |  .mockSubProcess("myOtherSubProcess") // adds a SubProcess""".stripMargin,
        """"mockedSubprocesses": ["mySubProcess", "myOtherSubProcess"],"""
      ) +
      "### Processes and ServiceProcesses" +
      createGeneralVariable(
        InputParams.outputVariables,
        s"""You can filter the Output with a list of variable names you are interested in.
           |This list may include all variables from the output (`Out`). We included an example for each Process or ServiceProcess.
           |${listOfStringsOrCommaSeparated("name,firstName")}
           |""".stripMargin,
        """process(..) // or serviceProcess(..)
          |  .withOutputVariables("name", "firstName") // creates a list with outputVariables
          |  .withOutputVariable("nickname") // adds a outputVariable""".stripMargin,
        """"outputVariables": ["name", "firstName"],"""
      ) +
      createGeneralVariable(
        InputParams.outputMock,
        """Mock the Process or Service (`Out`)
          | - You find an example in every _Process_ and _ServiceProcess_.
          |""".stripMargin,
        """process(..) // or serviceProcess(..)
          |  .mockWith(outputMock)""".stripMargin,
        """"outputMock": {..},"""
      ) +
      createGeneralVariable(
        InputParams.servicesMocked,
        "Mock the ServiceProcesses (Workers only) with their default Mock:",
        "process(..) // or serviceProcess(..)\n  .mockServices",
        "\"servicesMocked\": true,"
      ) +
      createGeneralVariable(
        InputParams.impersonateUserId,
        """User-ID of a User that should be taken to authenticate to the services.
          |This must be supported by your implementation. *Be caution: this may be a security issue!*.
          |It is helpful if you have Tokens that expire, but long running Processes.""".stripMargin,
        """process(..) // or serviceProcess(..)
          |  .withImpersonateUserId(impersonateUserId)""".stripMargin,
        """"impersonateUserId": "myUserName","""
      ) +
      "### ServiceProcesses" +
      createGeneralVariable(
        InputParams.outputServiceMock,
        """Mock the Inner-Service (`MockedServiceResponse[ServiceOut]`)
          | - You find an example in every _ServiceProcess_.
          |""".stripMargin,
        """serviceProcess(..)
          |  .mockServiceWith(MockedServiceResponse
          |     .success200(inOut.defaultServiceMock))""".stripMargin,
        s""""outputServiceMock": ${MockedServiceResponse
          .success200("Example String Body")
          .asJson},""".stripMargin
      ) +
      createGeneralVariable(
        InputParams.handledErrors,
        s"""A list of error codes that are handled (`BpmnError`)
           |${listOfStringsOrCommaSeparated("validation-failed,404")}
           |""".stripMargin,
        """serviceProcess(..)
          |  .handleErrors(ErrorCodes.`validation-failed`, "404") // create a list of handledErrors
          |  .handleError("404") // add a handledError""".stripMargin,
        s""""handledErrors": ["validation-failed", "404"],""".stripMargin
      ) +
      createGeneralVariable(
        InputParams.regexHandledErrors,
        s"""You can further filter Handled Errors with a list of Regex expressions that the body error message must match.
           |${listOfStringsOrCommaSeparated(
          "SQL exception,\"errorNr\":\"20000\""
        )}
           |""".stripMargin,
        """serviceProcess(..)
          |  .handleErrorWithRegex("SQL exception")
          |  .handleErrorWithRegex("\"errorNr\":\"20000\"")""".stripMargin,
        s""""regexHandledErrors": ["SQL exception", "\"errorNr\":\"20000\""],""".stripMargin
      ) +
      """</p>
        |</details>
        |<p/>
        """.stripMargin
  end createGeneralVariables

  def createGeneralVariable(
      key: InputParams,
      descr: String,
      scalaExample: String,
      jsonExample: String
  ) =
    if (supportedVariables.contains(key))
      s"""
         |**$key**:
         |
         |$descr
         |
         |- DSL:
         |```scala
         |$scalaExample
         |```
         |
         |- Json
         |```json
         |...
         |$jsonExample
         |...
         |```
         |
         |""".stripMargin
    else
      ""
  end createGeneralVariable

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
       |**See the [Camundala Documentation](https://pme123.github.io/camundala/)
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

  private def listOfStringsOrCommaSeparated(example: String) =
    s"""Depending on your implementation it is also possible to use a _comma separated_ String,
       |like `"$example"`""".stripMargin
end ApiCreator
