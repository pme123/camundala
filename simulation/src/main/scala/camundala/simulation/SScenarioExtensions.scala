package camundala.simulation

import camundala.api.*
import camundala.api.CamundaVariable.CInteger
import camundala.bpmn.*
import io.circe.parser.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*

trait SScenarioExtensions extends SimulationHelper:

  extension (scenario: SScenario)
    def process = scenario.process

    def correlate(
        msgName: String,
        businessKey: Option[String] = None
    ): ChainBuilder =
      exec(
        http(scenario.description("Correlate Message", msgName))
          .post(s"/message")
          .auth()
          .body(
            StringBody(
              CorrelateMessageIn(
                messageName = msgName,
                tenantId = implicitly[SimulationConfig].tenantId,
                businessKey = businessKey,
                processVariables = Some(process.camundaInMap)
              ).asJson.toString
            )
          ) // Camunda returns different results depending if the process is running!
          .check {
            extractJsonOptional("$[*].processInstance.id", "processInstanceId")
          }
          .check {
            extractJsonOptional(
              "$[*].execution.processInstanceId",
              "processInstanceId2"
            )
          }
      ).exitHereIfFailed
        .exec { session =>
          session.set(
            "processInstanceId",
            session.attributes
              .get("processInstanceId2")
              .orElse(session.attributes.get("processInstanceId"))
              .getOrElse("NOT-SET")
          )
        }

    def start(
        expectedStatus: Int = 200,
        expectedMsg: Option[String] = None
    ): ChainBuilder =
      val tenantId = config.tenantId
      exec(
        http(scenario.description("Start", scenario.name))
          .post(s"/process-definition/key/${process.id}${tenantId
            .map(id => s"/tenant-id/$id")
            .getOrElse("")}/start")
          .auth()
          .body(
            StringBody(
              StartProcessIn(
                process.camundaInMap,
                businessKey = Some(scenario.name)
              ).asJson.deepDropNullValues.toString
            )
          )
          .check(status.is(expectedStatus))
          .checkIf(expectedMsg.nonEmpty) {
            substring(expectedMsg.getOrElse("---"))
          }
          .checkIf(expectedStatus == 200) {
            extractJson("$.id", "processInstanceId")
          }
          .checkIf(expectedStatus == 200) {
            extractJson("$.businessKey", "businessKey")
          }
      ).exitHereIfFailed

  extension (
      scenario: (SScenario | SSubProcess)
  )
    def name = scenario match
      case ss: SScenario => ss.name
      case ss: SSubProcess => ss.name

    def process = scenario match
      case ss: SScenario => ss.process
      case ss: SSubProcess => ss.inOut

    def check(
    ): Seq[ChainBuilder] = {
      Seq(
        exec(_.set("processState", null)),
        retryOrFail(
          exec(checkFinished()).exitHereIfFailed,
          processFinishedCondition
        ),
        exec(checkVars()).exitHereIfFailed
      )
    }

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

    def checkVars(): HttpRequestBuilder =
      http(description("Check", scenario.name))
        .get(
          "/history/variable-instance?processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
        )
        .auth()
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[Seq[CamundaProperty]]) match {
                case Right(value) =>
                  checkProps(scenario.asInstanceOf[WithTestOverrides[_]], value)
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of CamundaProperty.\n$exc\n$body"
              }
            }
            .is(true)
        )

    def checkFinished(): HttpRequestBuilder =
      http(description("Check finished", scenario.name))
        .get(s"/history/process-instance/#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(extractJson("$.state", "processState"))

    private inline def description(
        prefix: String,
        scenarioName: String
    ): String =
      val d =
        if (scenarioName == process.id) scenarioName
        else s"'$scenarioName' (${process.id})"
      s"$prefix $d"

  end extension
