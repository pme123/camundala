package camundala.simulation
package custom

import camundala.bpmn.*
import io.circe.*
import sttp.client3.*

trait SUserTaskExtensions extends SimulationHelper:

  extension (userTask: SUserTask)

    def getAndComplete()(using data: ScenarioData): ResultType =
      given ScenarioData = data.withTaskId(notSet)
      for {
        given ScenarioData <- task()
        given ScenarioData <- checkForm()
        given ScenarioData <- userTask.waitForSec.map(userTask.waitFor).getOrElse(Right(summon[ScenarioData]))
        given ScenarioData <- completeTask()
      } yield summon[ScenarioData]

    private def task()(using data: ScenarioData): ResultType = {
      def getTask(
          processInstanceId: Any
      )(data: ScenarioData): ResultType = {
        val uri =
          uri"${config.endpoint}/task?processInstanceId=$processInstanceId"
        val request = basicRequest
          .auth()
          .get(uri)
        given ScenarioData = data
          .info(
            s"UserTask '${userTask.name}' get"
          )
          .debug(s"- URI: $uri")

        request
          .extractBody()
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
                userTask.tryOrFail(getTask(processInstanceId))
              }
          )
      }

      val processInstanceId = data.context.processInstanceId
      getTask(processInstanceId)(data.withRequestCount(0))
    }

    def checkForm()(using data: ScenarioData): ResultType = {
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/process-instance/$processInstanceId/variables?deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)

      given ScenarioData = data
        .info(
          s"UserTask '${userTask.name}' checkForm"
        )
        .debug(s"- URI: $uri")

      request
        .extractBody()
        .flatMap(
          _.as[FormVariables].left
            .map(err =>
              summon[ScenarioData]
                .error(s"Problem creating FormVariables from response.\n$err")
            )
        )
        .flatMap(formVariables =>
          if (
            checkProps(
              userTask,
              CamundaProperty.from(formVariables)
            )
          )
            Right(
              summon[ScenarioData]
                .info(s"UserTask Form is correct for ${userTask.name}")
            )
          else
            Left(
              summon[ScenarioData].error(
                s"Tests for UserTask Form ${userTask.name} failed - check log above (look for !!!)"
              )
            )
        )
    }

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
        .debug(s"- URI: $uri")

      val response = request.send(backend)
      response.body.left
        .map(body => handleNon2xxResponse(response.code, body, request.toCurl))
        .map(_ =>
          summon[ScenarioData]
            .info(s"Successful completed UserTask ${userTask.name}.")
        )

    end completeTask

  end extension
