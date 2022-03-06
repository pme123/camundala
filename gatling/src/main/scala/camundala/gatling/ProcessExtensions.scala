package camundala
package gatling

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

trait ProcessExtensions:
  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      process: Process[In, Out]
  )
  
    def correlate(
        msgName: String = process.id,
        businessKey: Option[String] = None
    ): WithConfig[ChainBuilder] =
      exec(
        http(description("Correlate Message", msgName))
          .post(s"/message")
          .auth()
          .body(
            StringBody(
              CorrelateMessageIn(
                messageName = msgName,
                tenantId = implicitly[SimulationConfig].tenantId,
                businessKey = businessKey,
                processVariables = Some(CamundaVariable.toCamunda(process.in))
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
  
    def start(scenario: String, expectedStatus: Int = 200): WithConfig[ChainBuilder] =
      val tenantId = summon[SimulationConfig].tenantId
      exec(
        http(description("Start", scenario))
          .post(s"/process-definition/key/${process.id}${tenantId
            .map(id => s"/tenant-id/$id")
            .getOrElse("")}/start")
          .auth()
          .body(
            StringBody(
              StartProcessIn(
                CamundaVariable.toCamunda(process.in),
                businessKey = Some(scenario)
              ).asJson.deepDropNullValues.toString
            )
          )
          .check(status.is(expectedStatus))
          .checkIf(expectedStatus == 200) {
            extractJson("$.id", "processInstanceId")
          }
          .checkIf(expectedStatus == 200) {
            extractJson("$.businessKey", "businessKey")
          }
      ).exitHereIfFailed
  
    def exists(
        key: String
    ): Process[In, TestOverrides] =
      processOverride(key, TestOverrideType.Exists)
  
    def notExists(
        key: String
    ): Process[In, TestOverrides] =
      processOverride(key, TestOverrideType.NotExists)
  
    def isEquals(
        key: String,
        value: Any
    ): Process[In, TestOverrides] =
      processOverride(
        key,
        TestOverrideType.IsEquals,
        Some(CamundaVariable.valueToCamunda(value))
      )
  
    def hasSize(
        key: String,
        size: Int
    ): Process[In, TestOverrides] =
      processOverride(
        key,
        TestOverrideType.HasSize,
        Some(CInteger(size))
      )
  
    def processOverride(
        key: String,
        overrideType: TestOverrideType,
        value: Option[CamundaVariable] = None
    ): Process[In, TestOverrides] =
      Process(
        InOutDescr(
          process.id,
          process.in,
          addOverride(process, key, overrideType, value),
          process.descr
        )
      )
  
    def check(
        scenario: String = ""
    ): WithConfig[Seq[ChainBuilder]] = {
      Seq(
        exec(_.set("processState", null)),
        retryOrFail(
          exec(checkFinished(scenario)).exitHereIfFailed,
          processFinishedCondition
        ),
        exec(checkVars(scenario)).exitHereIfFailed
      )
    }
  
    // checks if a variable has this value.
    // it tries up to the time defined.
    def checkRunningVars(
        variable: String,
        value: Any
    ): WithConfig[Seq[ChainBuilder]] = {
      Seq(
        exec(_.set(variable, null)),
        retryOrFail(
          loadVariable(variable),
          processReadyCondition(variable, value)
        )
      )
    }
  
    def checkVars(
        scenario: String
    ): WithConfig[HttpRequestBuilder] =
      http(description("Check", scenario))
        .get(
          "/history/variable-instance?processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
        )
        .auth()
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[Seq[CamundaProperty]]) match {
                case Right(value) => checkProps(process.out, value)
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of CamundaProperty.\n$exc\n$body"
              }
            }
            .is(true)
        )
  
    def checkFinished(scenario: String): WithConfig[HttpRequestBuilder] =
      http(description("Check finished", scenario))
        .get(s"/history/process-instance/#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(extractJson("$.state", "processState"))
  
    def switchToCalledProcess(): WithConfig[ChainBuilder] =
      exec(session =>
        session.set(
          "processInstanceIdBackup",
          session("processInstanceId").as[String]
        )
      ).exec(
        http(s"Switch to Called Process of ${process.id}")
          .get(s"/process-instance?superProcessInstance=#{processInstanceId}")
          .auth()
          .check(extractJson("$[*].id", "processInstanceId"))
      )
  
    def switchToMainProcess(): ChainBuilder =
      exec(session =>
        session.set(
          "processInstanceId",
          session("processInstanceIdBackup").as[String]
        )
      )

    private inline def description(prefix: String, scenario: String): String =
      val d = if(scenario == process.id) scenario else s"'$scenario' (${process.id})"
      s"$prefix $d"

  end extension
