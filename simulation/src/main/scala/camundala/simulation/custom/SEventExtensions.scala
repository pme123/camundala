package camundala.simulation
package custom

import camundala.api.*
import camundala.bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*

trait SEventExtensions extends SimulationHelper:

  extension (sEvent: SEvent)

    def loadVariable()(using data: ScenarioData): ResultType = {
      val variableName = sEvent.readyVariable
      val readyValue = sEvent.readyValue
      def loadVariable(
                   processInstanceId: Any
                 )(data: ScenarioData): ResultType = {
        val uri =
          uri"${config.endpoint}/history/variable-instance?variableName=$variableName&processInstanceId=$processInstanceId&deserializeValues=false"
        val request = basicRequest
          .auth()
          .get(uri)
        given ScenarioData = data
        runRequest(request, s"${sEvent.inOut.getClass.getSimpleName} '${sEvent.name}' loadVariables")(
          (body, data) =>
            body.hcursor.downArray
              .downField("value")
              .as[Json]
              .flatMap { value =>
                if(value.toString == readyValue.toString)
                  Right(data
                    .info(
                      s"Variable for '${sEvent.name}' ready ($variableName = '$readyValue')"
                    ))
                else
                  Left (data
                    .info(s"Variable found for '${sEvent.name}' but not ready ($variableName = '$readyValue' (result: '$value'))")
                  )
              }
              .left
              .flatMap { _ =>
                tryOrFail(loadVariable(processInstanceId), sEvent)
              }
          )
      }

      val processInstanceId = data.context.processInstanceId
      loadVariable(processInstanceId)(data.withRequestCount(0))
    }

  end extension

  extension (sEvent: SReceiveMessageEvent)
    def event = sEvent.inOut

    def sendMessage()(using ScenarioData): ResultType =
      if (sEvent.optReadyVariable.nonEmpty) {
        for {
          given ScenarioData <- sEvent.loadVariable()
          given ScenarioData <- sendMsg()
        } yield summon[ScenarioData]
      } else // default: try until it returns status 200
        sendMsg()

    def sendMsg()(using data: ScenarioData): ResultType = {
      def correlate()(data: ScenarioData): ResultType =
        val processInstanceId: Option[String] =
          if (sEvent.processInstanceId) Some(data.context.processInstanceId)
          else None
        val tenant = // only set if there is no processInstanceId
          if (sEvent.processInstanceId) None
          else summon[SimulationConfig[?]].tenantId
        val body = CorrelateMessageIn(
          messageName = event.messageName,
          tenantId = tenant,
          processInstanceId = processInstanceId,
          processVariables = Some(event.camundaInMap)
        ).asJson.deepDropNullValues.toString
        val uri = uri"${config.endpoint}/message"

        val request = basicRequest
          .auth()
          .contentType("application/json")
          .body(body)
          .post(uri)

        given ScenarioData = data

        val response = request.send(backend)
        response.body
          .map { (body: String) =>
            summon[ScenarioData]
              .info(
                s"Message '${sEvent.name}' received"
              )
              .debug(s"- body: $body")
          }.left
          .flatMap { _ =>
            tryOrFail(correlate(), sEvent)
          }
      end correlate

      correlate()(summon[ScenarioData])
    }
  end extension // SReceiveMessageEvent

  extension (sEvent: SReceiveSignalEvent)
    def event = sEvent.inOut
    def sendSignal()(using ScenarioData): ResultType =
      for {
        given ScenarioData <- sEvent.loadVariable()
        given ScenarioData <- sndSgnl()
      } yield summon[ScenarioData]

    private def sndSgnl()(using
        data: ScenarioData
    ): ResultType = {
      val body = SendSignalIn(
        name = event.messageName,
        variables = Some(event.camundaInMap)
      ).asJson.deepDropNullValues.toString
      val uri = uri"${config.endpoint}/signal"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      runRequest(request, s"Signal '${sEvent.name}' sent")((_, data) =>
        Right(data)
      )
    }
  end extension // SReceiveSignalEvent

end SEventExtensions
