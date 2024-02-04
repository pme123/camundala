package camundala.api

import camundala.api.docs.ApiProjectConf
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
    ModelerTemplGenerator(version, apiConfig.modelerTemplateConfig, Some(projectName)).generate(
      collectApis(apiDoc)
    )
    ModelerTemplUpdater(apiConfig).update()
    writeOpenApis(apiDoc)
    writeCatalog(apiDoc)
  end document

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
  end writeOpenApis

  protected lazy val openAPIDocsInterpreter =
    OpenAPIDocsInterpreter(docsOptions =
      OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
    )

  protected def openApi(apiDoc: ApiDoc): OpenAPI =
    val endpoints = create(apiDoc)
    openAPIDocsInterpreter
      .toOpenAPI(endpoints, info(title, description))
  end openApi

  protected def postmanOpenApi(apiDoc: ApiDoc): OpenAPI =
    val endpoints = createPostman(apiDoc)
    openAPIDocsInterpreter
      .toOpenAPI(endpoints, info(title, postmanDescription))
      .servers(servers)
  end postmanOpenApi

  protected def createChangeLog(): String =
    val changeLogFile = basePath / "CHANGELOG.md"
    if changeLogFile.toIO.exists() then
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
    end if
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
        |### Mocking
        |""".stripMargin +
      createGeneralVariable(
        InputParams.servicesMocked,
        "Mock all the _ServiceWorkers_ in your process with their default Mock:",
        "process(..)\n  .mockServices",
        s"\"${InputParams.servicesMocked}\": true,"
      ) +
      createGeneralVariable(
        InputParams.mockedWorkers,
        s"""Mock any Process- and/or ExternalTask-Worker with their default Mocks.
           |This is a list of the _Worker topicNames or Process processNames_, you want to mock.
           |${listOfStringsOrCommaSeparated("mySubProcess,myOtherSubProcess,myService")}
           |
           |_Be aware_: For Sub-Processes, this expects an _InitWorker_ where the _topicName_ is equal to the _processName_.
           |""".stripMargin,
        """process(..)
          |  .mockedWorkers("mySubProcess1", "mySubProcess2") // creates a list with SubProcessess
          |  .mockedWorker("myOtherSubProcess") // adds a SubProcess""".stripMargin,
        """"mockedWorkers": ["mySubProcess", "myOtherSubProcess, myService"],"""
      ) +
      createGeneralVariable(
        InputParams.outputMock,
        """Mock the Process or ExternalTask (`Out`)
          | - You find an example in every _Process_ and _ExternalTask_.
          |""".stripMargin,
        """process(..) // or serviceTask(..)/customTask(..)
          |  .mockWith(outputMock)""".stripMargin,
        """"outputMock": {..},"""
      ) +
      createGeneralVariable(
        InputParams.outputServiceMock,
        """Mock the Inner-Service (`MockedServiceResponse[ServiceOut]`)
          | - You find an example in every _ServiceTask_.
          |""".stripMargin,
        """serviceTask(..)
          |  .mockServiceWith(MockedServiceResponse
          |     .success200(inOut.defaultServiceOutMock))""".stripMargin,
        s""""outputServiceMock": ${MockedServiceResponse
            .success200("Example String Body")
            .asJson},""".stripMargin
      ) +
      "### Mapping" +
      createGeneralVariable(
        InputParams.outputVariables,
        s"""You can filter the Output with a list of variable names you are interested in.
           |This list may include all variables from the output (`Out`). We included an example for each Process or ExternalTask.
           |${listOfStringsOrCommaSeparated("name,firstName")}
           |""".stripMargin,
        """process(..) // or serviceTask(..)/customTask(..)
          |  .withOutputVariables("name", "firstName") // creates a list with outputVariables
          |  .withOutputVariable("nickname") // adds a outputVariable""".stripMargin,
        """"outputVariables": ["name", "firstName"],"""
      ) +
      createGeneralVariable(
        InputParams.manualOutMapping,
        s"""By default all output Variables (`Out`) are on the Process for _External Tasks_.
           |If the filter _${InputParams.outputVariables}_ is not enough,
           |you can set this variable - every output variable is then local.
           |
           |_Be aware_ that you must then manually have _output mappings_ for each output variable!
           |""".stripMargin,
        """serviceTask(..) // or customTask(..)
          |  .manualOutMapping""".stripMargin,
        """"manualOutMapping": true,"""
      ) + "### Mocking" +
      createGeneralVariable(
        InputParams.handledErrors,
        s"""A list of error codes that are handled (`BpmnError`)
           |${listOfStringsOrCommaSeparated("validation-failed,404")}
           |
           |At the moment only _ServiceTasks_ supported.
           |""".stripMargin,
        """serviceTask(..)
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
           |
           |At the moment only _ServiceTasks_ supported.
           |""".stripMargin,
        """serviceTask(..)
          |  .handleErrorWithRegex("SQL exception")
          |  .handleErrorWithRegex("\"errorNr\":\"20000\"")""".stripMargin,
        s""""regexHandledErrors": ["SQL exception", "\"errorNr\":\"20000\""],""".stripMargin
      ) +
      "### Authorization" +
      createGeneralVariable(
        InputParams.impersonateUserId,
        """User-ID of a User that should be taken to authenticate to the services.
          |This must be supported by your implementation. *Be caution: this may be a security issue!*.
          |It is helpful if you have Tokens that expire, but long running Processes.""".stripMargin,
        """process(..) // or serviceTask(..)/customTask(..)
          |  .withImpersonateUserId(impersonateUserId)""".stripMargin,
        """"impersonateUserId": "myUserName","""
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
    if supportedVariables.contains(key) then
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
    if readme.toIO.exists() then
      os.read.lines(readme).tail.mkString("\n")
    else
      "There is no README.md in the Project."
  end createReadme

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
    description.map(descr =>
      s"""
         |**This is for Postman - to have example requests. Be aware the Output is not provided!**
         |
         |$descr
         |""".stripMargin
    )

  private def writeOpenApi(
      path: os.Path,
      api: OpenAPI,
      docPath: os.Path
  ): Unit =
    if os.exists(path) then
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")
    println(s"See Open API Html $docPath")
  end writeOpenApi

  private def writeCatalog(apiDoc: ApiDoc): Unit =
    val catalogPath = apiConfig.catalogPath
    if os.exists(catalogPath) then
      os.remove(catalogPath)
    val catalog = toCatalog(apiDoc)
    os.write(catalogPath, catalog)
    println(s"Created Catalog $catalogPath")
  end writeCatalog

  private def toCatalog(apiDoc: ApiDoc): String =
    val optimizedApis = collectApisWithGroup(apiDoc)
    s"""### $title
       |${toCatalog(optimizedApis)}
       |""".stripMargin
  end toCatalog

  // takes exactly one api
  private def collectApisWithGroup(apiDoc: ApiDoc): List[(InOutApi[?, ?], String)] =
    apiDoc.apis.foldLeft(List.empty[(InOutApi[?, ?], String)]) {
      case (result, groupedApi: ProcessApi[?, ?]) =>
        result ++ (groupedApi.apis :+ groupedApi ).map(_ -> groupedApi.name)
      case (result, groupedApi: GroupedApi) =>
        result ++ groupedApi.apis.map(_ -> groupedApi.name)
      case (result, _) =>
        result
    }.distinct

  // takes exactly one api
  private def collectApis(apiDoc: ApiDoc): List[InOutApi[?, ?]] =
    collectApisWithGroup(apiDoc)
      .map:
        _._1

  private def toCatalog(
      apis: List[(InOutApi[?, ?], String)]
  ): String =
    apis
      .map:
        case api -> anchor =>
          s"- ${createLink(api.endpointName, Some(anchor))}"
      .sorted
      .mkString("\n")
  end toCatalog

  private def listOfStringsOrCommaSeparated(example: String) =
    s"""It is also possible to use a _comma separated_ String,
       |like `"$example"`""".stripMargin
end ApiCreator
