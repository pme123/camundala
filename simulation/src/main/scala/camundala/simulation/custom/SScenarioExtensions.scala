package camundala.simulation
package custom

import camundala.api.*
import camundala.bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*

import scala.collection.immutable.Seq

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

      request
        .extractBody()
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
          given ScenarioData <- checkIncident()(summon[ScenarioData])
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
                body.hcursor.downArray
                  .downField("incidentMessage")
                  .as[String]
                  .left
                  .map { ex =>
                    data
                      .error(
                        s"Problem extracting incidentMessage from $body\n $ex"
                      )
                  }
                  .flatMap {
                    case incidentMessage
                        if incidentMessage.contains(scen.incidentMsg) =>
                      Right(
                        data
                          .info(
                            s"Process ${scen.name} has finished with incident (as expected)."
                          )
                      )
                    case incidentMessage =>
                      Left(
                        data.error(
                          "The Incident contains not the expected message." +
                            s"\nExpected: ${scen.incidentMsg}\nActual Message: $incidentMessage"
                        )
                      )
                  }
              case _ =>
                given ScenarioData = data
                tryOrFail(checkIncident(), scen)
            }
            .getOrElse(
              Left(data.error("An Array is expected (should not happen)."))
            )
      }
    }
    end checkIncident

  end extension

  extension (scen: DmnScenario)
    def run(): ResultType =
      scen.logScenario { (data: ScenarioData) =>
        given ScenarioData = data
        evaluate()
      }

    def evaluate()(using data: ScenarioData): ResultType = {
      val dmn = scen.inOut
      val backend = HttpClientSyncBackend()
      val body = EvaluateDecisionIn(
        dmn.camundaInMap
      ).asJson.toString
      val uri =
        uri"${config.endpoint}/decision-definition/key/${dmn.decisionDefinitionKey}${config.tenantPath}/evaluate"

      given ScenarioData = data
        .info(s"URI: $uri")
        .debug(s"Body: $body")

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      request
        .extractBody()
        .flatMap { body =>
          body
            .as[Seq[Map[String, CamundaVariable]]]
            .left
            .map { ex =>
              summon[ScenarioData]
                .error(
                  s"Problem extracting Seq[Map[String, CamundaVariable] from $body\n $ex"
                )
            }
            .flatMap { values =>
              evaluateDmn(values)
            }

        }
    }
    private def evaluateDmn(resultSeq: Seq[Map[String, CamundaVariable]])(using
        data: ScenarioData
    ): ResultType =
      val result = resultSeq.map(
        _.filter(_._2 != CamundaVariable.CNull)
      )
      val decisionDmn: DecisionDmn[_, _] = scen.inOut
      val check: ResultType = decisionDmn.out match
        case expected: SingleEntry[_] =>
          for {
            given ScenarioData <-
              if (result.size == 1 && result.head.size == 1)
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if (result.head.head._2 == expected.toCamunda)
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head.head._2}"
                  )
                )
          } yield summon[ScenarioData]
        case expected: SingleResult[_] =>
          for {
            given ScenarioData <-
              if (
                result.size == 1 &&
                result.head.size > 1
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"A ${expected.decisionResultType} is expected, but it was: $result"
                  )
                )
            given ScenarioData <-
              if (result.head == expected.toCamunda) Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: ${result.head}"
                  )
                )
          } yield summon[ScenarioData]
        case expected: CollectEntries[_] =>
          val resultValues = result.map(_.values.head)
          scen.testOverrides match
            case None =>
              if (
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size == 1 &&
                  resultValues.toSet == expected.toCamunda.toSet)
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $resultValues"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if (checkOForCollection(overrides, resultValues))
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $resultValues"
                  )
                )

        case expected: ResultList[_] =>
          scen.testOverrides match
            case None =>
              if (
                (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size > 1 &&
                  result.toSet == expected.toCamunda.toSet)
              )
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} did not match, \n- expected: ${expected.toCamunda} \n- result: $result"
                  )
                )
            case Some(testOverrides) =>
              val overrides = testOverrides.overrides
              if (checkOForCollection(overrides, result))
                Right(summon[ScenarioData])
              else
                Left(
                  summon[ScenarioData].error(
                    s"The ${expected.decisionResultType} with TestOverrides did not match, \n- expected: $overrides \n- result: $result"
                  )
                )
        case _ =>
          Left(
            summon[ScenarioData].error(
              s"Unknown Type ${decisionDmn.out.getClass}: ${decisionDmn.out}"
            )
          )

      check.map(data =>
        data.info(s"Dmn Evaluation was successful for ${scen.name}")
      )
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

  extension (scen: SScenario)
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

end SScenarioExtensions
