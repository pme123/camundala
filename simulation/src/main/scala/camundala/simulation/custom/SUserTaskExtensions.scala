package camundala.simulation.custom

import camundala.api.*
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*

import scala.util.Try

trait SUserTaskExtensions extends SimulationHelper:

  extension (userTask: SUserTask)

    def getAndComplete()(using data: ScenarioData): ResultType =
      given ScenarioData = data.withTaskId(notSet)
      for {
        given ScenarioData <- task()
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
                    s"Task '${userTask.name}' ready"
                  )
                  .info(s"- taskId: $taskId")
                  .debug(s"- body: $body")
              }
              .left
              .flatMap { ex =>
                  tryOrFail(getTask(processInstanceId))
              }
          )
      }

      val processInstanceId = data.context.processInstanceId
      getTask(processInstanceId)(data.withRequestCount(0))
        .left.map(msg => data.error(msg.toString))
    }
    private def tryOrFail(funct: ScenarioData => ResultType)(using data: ScenarioData)= {
      val count = summon[ScenarioData].context.requestCount
      if (count < config.maxCount) {
        Try(Thread.sleep(1000)).toEither.left
          .map(_ =>
            summon[ScenarioData]
              .error("Interrupted Exception when waiting.")
          )
          .flatMap { _ =>
            funct(
              summon[ScenarioData]
                .withRequestCount(count + 1)
                .debug(s"Waiting for UserTask (count: $count)")
            )
          }
      } else {
        Left(
          summon[ScenarioData]
            .error(s"Expected Task was not found! Tried $count times.")
        )
      }
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
                ) */
  end extension
