package camundala
package simulation

import camundala.api.*
import camundala.bpmn.*
import io.circe.parser.parse
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

trait SSubProcessExtensions extends SimulationHelper:

  extension (process: SSubProcess)

    def switchToSubProcess(): Seq[ChainBuilder] =
      Seq(
        exec(session =>
          session.set(
            "processInstanceIdBackup",
            session("processInstanceId").as[String]
          )
        ),
        exec(_.set("processInstanceId", null)),
        retryOrFail(
          exec(processInstance()).exitHereIfFailed,
          processInstanceCondition()
        )
      )

    def switchToMainProcess(): ChainBuilder =
      exec(session =>
        session.set(
          "processInstanceId",
          session("processInstanceIdBackup").as[String]
        )
      )
    private def processInstance(): HttpRequestBuilder =
      http(s"Switch to '${process.name}'")
        .get(
          s"/process-instance?superProcessInstance=#{processInstanceIdBackup}&active=true&processDefinitionKey=${process.id}"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].id", "processInstanceId")
        )
  end extension
