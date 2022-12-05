package camundala.simulation
package custom

import camundala.api.*
import io.circe.*
import io.circe.parser.parse
import io.circe.syntax.*
import sttp.client3.*

trait SScenarioExtensions extends SStepExtensions:

  extension (scen: IsProcessScenario)

    def startProcess()(using
        data: ScenarioData
    ): ResultType = {
      val process = scen.process
      val backend = HttpClientSyncBackend()
      val body = StartProcessIn(
        process.camundaInMap,
        businessKey = Some(scen.name)
      ).asJson.deepDropNullValues.toString
      val uri =
        uri"${config.endpoint}/process-definition/key/${process.id}${config.tenantPath}/start"

      given ScenarioData = data
        .info(s"URI: $uri")
        .debug(s"Body: $body")

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      val response = request.send(backend)
      response.body.left
        .map(body => handleNon2xxResponse(response.code, body, request.toCurl))
        .flatMap(parser.parse)
        .left
        .map(err =>
          summon[ScenarioData]
            .error(s"Problem creating body from response.\n$err")
        )
        .flatMap { body =>
          body.hcursor
            .downField("id")
            .as[String]
            .map { processInstanceId =>
              summon[ScenarioData]
                .withProcessInstanceId(processInstanceId)
                .info(
                  s"Process '${process.processName}' started"
                )
                .debug(s"- processInstanceId: $processInstanceId")
                .debug(s"- body: $body")
            }
            .left
            .map { ex =>
              summon[ScenarioData]
                .error(s"Problem extracting processInstanceId from $body\n $ex")
            }
        }
    }
    def runSteps()(using
        data: ScenarioData
    ): ResultType =
      scen.steps.foldLeft[ResultType](Right(data)) {
        case (Right(data), step) =>
          given ScenarioData = data

          step.run()
        case (leftData, _) => leftData
      }

    def logScenario(body: ScenarioData => ResultType): ResultType =
      val data = ScenarioData(logEntries =
        Seq(info(s"${"#" * 7} Scenario '${scen.name}' ${"#" * 7}"))
      )
      body(data)
        .map(
          _.info(
            s"${Console.GREEN}${"*" * 4} Scenario '${scen.name}' SUCCEEDED ${"*" * 4}${Console.RESET}"
          )
        )
        .left
        .map(
          _.error(
            s"${Console.RED}${"*" * 3} Scenario '${scen.name}' FAILED ${"*" * 3}${Console.RESET}"
          )
        )
    end logScenario

  end extension

  extension (scen: ProcessScenario)
    def run(): ResultType =
      scen.logScenario { (data: ScenarioData) =>
        given ScenarioData = data
        for
          given ScenarioData <- scen.startProcess()
          given ScenarioData <- scen.runSteps()
          given ScenarioData <- scen.check()
        yield summon[ScenarioData]
      }
  end extension

  extension (scen: IncidentScenario)
    def run(): ResultType =
      scen.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        for
          given ScenarioData <- scen.startProcess()
          given ScenarioData <- scen.runSteps()
          given ScenarioData <- scen.checkIncident()(summon[ScenarioData])
        yield summon[ScenarioData]
      }

    def checkIncident()(data: ScenarioData): ResultType = {
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/incident?processInstanceId=$processInstanceId&deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)
      given ScenarioData = data
      runRequest(request, s"Process '${scen.name}' checkIncident") {
        (body, data) =>
          body.hcursor.values
            .map {
              case (values: Iterable[Json]) if values.toSeq.nonEmpty =>
                body.hcursor
                  .downArray
                  .downField("incidentMessage")
                  .as[String]
                  .left
                  .map { ex =>
                    data
                      .error(s"Problem extracting incidentMessage from $body\n $ex")
                  }
                  .flatMap {
                    case incidentMessage if incidentMessage.contains(scen.incidentMsg) =>
                      Right(
                        data
                          .info(s"Process ${scen.name} has finished with incident (as expected).")
                      )
                    case incidentMessage =>
                      Left(
                        data.error("The Incident contains not the expected message." +
                          s"\nExpected: ${scen.incidentMsg}\nActual Message: $incidentMessage")
                      )
                  }
              case _ =>
                given ScenarioData = data
                tryOrFail(checkIncident(), scen)
            }
            .getOrElse(
              Left(data.error("An Array is expected (should not happen)."))
            )
        /*  (for

            errorMsg <- body.hcursor.downField("incidentMessage").as[String]
          //  if scen.incidentMsg.contains(errorMsg)
          yield data.info(s"There is the expected Incident with msg: $errorMsg"))
            .left.map(ex =>
            data.error(s"Expected Incident was not correct! " +
              s"\nExpected: ${scen.incidentMsg}\nProcess Msg: $ex")
          )*/
      }
    }
    end checkIncident

  end extension

  extension (
      scenario: (ProcessScenario) // | SSubProcess)
  )

    def check()(using
        data: ScenarioData
    ): ResultType = {
      for
        given ScenarioData <- checkFinished()(data)
        given ScenarioData <- checkVars()
      yield summon[ScenarioData]
    }
    /*
    // checks if a variable has this value.
    // it tries up to the time defined.
    def checkRunningVars(
        variable: String,
        value: Any
    ): Seq[ChainBuilder] = {
      Seq(
        exec(_.set(variable, null)),
        retryOrFail(
          loadVariable(variable),
          processReadyCondition(variable, value)
        )
      )
    }
     */

    def checkVars()(using data: ScenarioData): ResultType =
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/history/variable-instance?processInstanceIdIn=$processInstanceId&deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)
      runRequest(request, s"Process '${scenario.name}' checkVars")(
        (body, data) =>
          body
            .as[Seq[CamundaProperty]]
            .flatMap { value =>
              if (
                checkProps(scenario.asInstanceOf[WithTestOverrides[_]], value)
              )
                Right(data.info("Variables successful checked"))
              else
                (
                  Left(
                    data.error(
                      "Variables do not match - see above in the Log (look for !!!)"
                    )
                  )
                )
            }
            .left
            .map(exc =>
              data
                .error(
                  s"!!! Problem parsing Result Body to a List of CamundaProperty.\n$exc"
                )
                .debug(s"Responce Body: $body")
            )
      )
    end checkVars

    def checkFinished()(data: ScenarioData): ResultType =
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/history/process-instance/$processInstanceId"
      val request = basicRequest
        .auth()
        .get(uri)
      given ScenarioData = data
      runRequest(request, s"Process '${scenario.name}' checkProcess")(
        (body, data) =>
          body.hcursor
            .downField("state")
            .as[String]
            .left
            .map { ex =>
              data
                .error(s"Problem extracting state from $body\n $ex")
            }
            .flatMap {
              case state if state == "COMPLETED" =>
                Right(
                  data
                    .info(s"Process ${scenario.name} has finished.")
                )
              case _ =>
                given ScenarioData = data
                tryOrFail(checkFinished(), scenario)
            }
      )
    end checkFinished

  end extension
end SScenarioExtensions
