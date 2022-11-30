package camundala.simulation.custom

import camundala.api.*
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*

trait SUserTaskExtensions extends SimulationHelper:

  extension (userTask: SUserTask)

    def getAndComplete()(using data: ScenarioData): ResultType =
      given ScenarioData = data.withTaskId(notSet)
      for {
        given ScenarioData <- task()
        given ScenarioData <- completeTask()
      } yield summon[ScenarioData]
    /*
        Seq(
          exec(_.set("taskId", null)),
          retryOrFail(
            exec(task()).exitHereIfFailed,
            taskCondition()
          ),
          exec(checkForm()).exitHereIfFailed,
          exec(completeTask()).exitHereIfFailed
        )
     */
    private def task()(using data: ScenarioData): ResultType = {

      def getTask(
          processInstanceId: Any
      )(data: ScenarioData): ResultType = {
        val backend = HttpClientSyncBackend()
        val uri =
          uri"${config.endpoint}/task?processInstanceId=$processInstanceId"
        val request = basicRequest
          .auth()
          .get(uri)
        given ScenarioData = data
          .info(
            s"UserTask '${userTask.name}' get"
          )
          .info(s"- URI: $uri")

        val response = request.send(backend)
        response.body
          .flatMap(parse)
          .left
          .map(body =>
            handleNon2xxResponse(response.code, body, request.toCurl)
          )
          .flatMap(body =>
            body.hcursor.downArray
              .downField("id")
              .as[String]
              .map { (taskId: String) =>
                summon[ScenarioData]
                  .withTaskId(taskId)
                  .info(
                    s"UserTask '${userTask.name}' ready"
                  )
                  .info(s"- taskId: $taskId")
                  .debug(s"- body: $body")
              }
              .left
              .flatMap { _ =>
                  tryOrFail(getTask(processInstanceId), userTask)
              }
          )
      }

      val processInstanceId = data.context.processInstanceId
      getTask(processInstanceId)(data.withRequestCount(0))
    }

  /*
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
*/
    private def completeTask()(using data: ScenarioData): ResultType =
      val taskId = data.context.taskId
      val backend = HttpClientSyncBackend()
      val uri =
        uri"${config.endpoint}/task/$taskId/complete?deserializeValues=false"
      val body = CompleteTaskOut(
        userTask.camundaOutMap
      ).asJson.deepDropNullValues.toString
      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)
      given ScenarioData = data
        .info(
          s"UserTask '${userTask.name}' complete"
        )
        .info(s"- URI: $uri")

      val response = request.send(backend)
      response.body
        .left
        .map(body =>
          handleNon2xxResponse(response.code, body, request.toCurl)
        )
        .map( _ =>
          summon[ScenarioData]
            .info(s"Successful completed UserTask ${userTask.name}.")
        )

    end completeTask

  end extension
