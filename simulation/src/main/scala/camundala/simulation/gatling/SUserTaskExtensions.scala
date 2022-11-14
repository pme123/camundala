package camundala
package simulation
package gatling

import camundala.api.*
import camundala.bpmn.*
import io.circe.parser.parse
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder
import io.circe.syntax.*

trait SUserTaskExtensions extends SimulationHelper:

  extension (userTask: SUserTask)

    def getAndComplete(): Seq[ChainBuilder] =
      Seq(
        exec(_.set("taskId", null)),
        retryOrFail(
          exec(task()).exitHereIfFailed,
          taskCondition()
        ),
        exec(checkForm()).exitHereIfFailed,
        exec(completeTask()).exitHereIfFailed
      )

    private def task(): HttpRequestBuilder =
      http(s"Get Tasks ${userTask.name}")
        .get("/task?processInstanceId=#{processInstanceId}")
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].id", "taskId")
        )

    private def checkForm(): HttpRequestBuilder =
      http(s"Check Form ${userTask.name}")
        .get(
          "/process-instance/#{processInstanceId}/variables?deserializeValues=false"
        )
        // Removed as Jsons were returned with type String?! Check History 8.1.22 19:00h
        // .get("/task/#{taskId}/form-variables?deserializeValues=false")
        .auth()
        .check(
          bodyString
            .transform { body =>
              parse(body)
                .flatMap(_.as[FormVariables]) match {
                case Right(value) =>
                  checkProps(
                    userTask,
                    CamundaProperty.from(value)
                  )
                case Left(exc) =>
                  s"\n!!! Problem parsing Result Body to a List of FormVariables.\n$exc\n$body"
              }
            }
            .is(true)
        )

    private def completeTask(): HttpRequestBuilder =
      http(s"Complete Task ${userTask.name}")
        .post(s"/task/#{taskId}/complete")
        .auth()
        .queryParam("deserializeValues", false)
        .body(
          StringBody(
            CompleteTaskOut(
              userTask.camundaOutMap
            ).asJson.deepDropNullValues.toString
          )
        )
  end extension
