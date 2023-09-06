package camundala.simulation
package custom
import sttp.client3.*


trait SSubProcessExtensions extends SimulationHelper:

  extension (process: SSubProcess)

    def switchToSubProcess()(using data: ScenarioData): ResultType =
      val superProcessInstanceId = data.context.processInstanceId

      def processInstance()(data: ScenarioData): ResultType = {
        val uri =
          uri"${config.endpoint}/process-instance?superProcessInstance=$superProcessInstanceId&active=true&processDefinitionKey=${process.processName}"
        val request = basicRequest
          .auth()
          .get(uri)

        given ScenarioData = data

        runRequest(request, s"Sub Process '${process.name}' processInstanceId")(
          (body, data) =>
            body.hcursor.downArray
              .downField("id")
              .as[String]
              .map { id =>
                summon[ScenarioData]
                  .withProcessInstanceId(id)
                  .info(
                    s"Switched to '${process.inOut.processName}' Sub Process (check $cockpitUrl/#/process-instance/$id)"
                  )
                  .debug(s"- processInstanceId: $id")
                  .debug(s"- body: $body")
              }
              .left
              .flatMap { _ =>
                process.tryOrFail(processInstance())
              }
        )
      }

      processInstance()(data.withRequestCount(0).switchToSubProcess())
    end switchToSubProcess

    def switchToMainProcess()(using data: ScenarioData): ResultType =
      Right(
        data
          .switchToMainProcess()
          .info(s"Switched back to Root Process.")
      )

  end extension
