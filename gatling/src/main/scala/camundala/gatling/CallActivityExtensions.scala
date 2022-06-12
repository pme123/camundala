package camundala.gatling

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

trait CallActivityExtensions:
  extension [
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      callActivity: CallActivity[In, Out]
  )
    def switchToSubProcess(
        subProcessName: String
    ): WithConfig[Seq[ChainBuilder]] = {
      Seq(
        exec(session =>
          session.set(
            "processInstanceIdBackup",
            session("processInstanceId").as[String]
          )
        ),
        exec(_.set("processInstanceId", null)),
        retryOrFail(
          exec(processInstance(subProcessName)).exitHereIfFailed,
          processInstanceCondition()
        )
      )
    }

    def switchToMainProcess(): ChainBuilder =
      exec(session =>
        session.set(
          "processInstanceId",
          session("processInstanceIdBackup").as[String]
        )
      )

    private def processInstance(
        subProcessName: String
    ): WithConfig[HttpRequestBuilder] =
      http(s"Switch to '$subProcessName'")
        .get(
          s"/process-instance?superProcessInstance=#{processInstanceIdBackup}&active=true&processDefinitionKey=${callActivity.subProcessId}"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].id", "processInstanceId")
        )
