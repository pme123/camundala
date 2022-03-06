package camundala.gatling

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

trait CallActivityExtensions:
  extension [
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
  ](
     callActivity: CallActivity[In, Out]
   )
    def switchToSubProcess(subProcessName: String): WithConfig[ChainBuilder] =
      exec(session =>
        session.set(
          "processInstanceIdBackup",
          session("processInstanceId").as[String]
        )
      ).exec(
        http(s"Switch to '$subProcessName'")
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
