package camundala.simulation

import camundala.api.*
import camundala.bpmn.CamundaVariable.CInteger
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
    def process = scenario.inOut

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

    def inOut = scenario match
      case ss: SScenario => ss.inOut
      case ss: SSubProcess => ss.inOut

    inline def description(
        prefix: String,
        scenarioName: String
    ): String =
      val inOut = scenario.inOut
      val d =
        if (scenarioName == inOut.id) scenarioName
        else s"'$scenarioName' (${inOut.id})"
      s"$prefix $d"

  extension (scenario: DmnScenario)
    def dmn = scenario.inOut

    def evaluate(): ChainBuilder =
      val tenantId = config.tenantId
      exec(
        http(scenario.description("Evaluate Decision DMN", dmn.id))
          .post(
            s"/decision-definition/key/${dmn.decisionDefinitionKey}${tenantId
              .map(id => s"/tenant-id/$id")
              .getOrElse("")}/evaluate"
          )
          .auth()
          .body(
            StringBody(
              EvaluateDecisionIn(
                dmn.camundaInMap
              ).asJson.toString
            )
          )
          .check(status.is(200))
          .check(
            bodyString
              .transform { body =>
                parse(body)
                  .flatMap(_.as[Seq[Map[String, CamundaVariable]]]) match {
                  case Right(values) =>
                    evaluateDmn(values)
                  case Left(exc) =>
                    s"\n!!! Problem parsing Result Body to a Seq of String -> CamundaVariable.\n$exc\n$body"
                }
              }
              .is(true)
          )
      ).exitHereIfFailed

    private def evaluateDmn(resultSeq: Seq[Map[String, CamundaVariable]]) =
      val result = resultSeq.map(
        _.filter(_._2 != CamundaVariable.CNull)
      )
      val decisionDmn: DecisionDmn[_, _] = scenario.inOut
      val check = decisionDmn.out match
        case expected: SingleEntry[_] =>
          val checkResult = result.size == 1 &&
            result.head.size == 1 &&
            result.head.head._2 == expected.toCamunda
          (
            checkResult,
            s"${expected.decisionResultType}): ${expected.toCamunda}"
          )
        case expected: SingleResult[_] =>
          val checkResult = result.size == 1 &&
            result.head.size > 1 &&
            result.head == expected.toCamunda
          (
            checkResult,
            s"${expected.decisionResultType}): ${expected.toCamunda}"
          )
        case expected: CollectEntries[_] =>
          scenario.testOverrides match
            case None =>
              val checkResult = (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size == 1 &&
                  result.map(_.values.head).toSet == expected.toCamunda.toSet)
              (
                checkResult,
                s"${expected.decisionResultType}): ${expected.toCamunda}"
              )
            case Some(testOverrides)  =>
              (
                checkOForCollection(testOverrides.overrides, result.map(_.values.head)),
                s"${expected.decisionResultType}): $testOverrides"
              )

        case expected: ResultList[_] =>
          scenario.testOverrides match
            case None =>
              val checkResult = (result.isEmpty && expected.toCamunda.isEmpty) ||
                (result.nonEmpty &&
                  result.head.size > 1 &&
                  result.toSet == expected.toCamunda.toSet)
              (
                checkResult,
                s"${expected.decisionResultType}): ${expected.toCamunda}"
              )
            case Some(testOverrides) =>
              (
                checkOForCollection(testOverrides.overrides, result),
                s"${expected.decisionResultType}): $testOverrides"
              )

        case _ =>
          (
            false,
            s"Unknown Type ${decisionDmn.out.getClass}: ${decisionDmn.out}"
          )

      if (check._1) check._1
      else
        s"\n!!! Dmn Evaluation failed:\n- Expected (${check._2}\n- Result: $result"

  extension (
      scenario: (ProcessScenario | SSubProcess)
  )

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
      http(scenario.description("Check", scenario.name))
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
      http(scenario.description("Check finished", scenario.name))
        .get(s"/history/process-instance/#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(extractJson("$.state", "processState"))

  end extension
