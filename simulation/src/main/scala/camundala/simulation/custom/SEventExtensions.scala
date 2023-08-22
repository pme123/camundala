package camundala.simulation
package custom

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
        runRequest(
          request,
          s"${sEvent.inOut.getClass.getSimpleName} '${sEvent.name}' loadVariables"
        )((body, data) =>
          body.hcursor.downArray
            .downField("value")
            .as[Json]
            .flatMap { value =>
              if (value.toString == readyValue.toString)
                Right(
                  data
                    .info(
                      s"Variable for '${sEvent.name}' ready ($variableName = '$readyValue')"
                    )
                )
              else
                Left(
                  data
                    .info(
                      s"Variable found for '${sEvent.name}' but not ready ($variableName = '$readyValue' (result: '$value'))"
                    )
                )
            }
            .left
            .flatMap { _ =>
              tryOrFail(loadVariable(processInstanceId), sEvent)
            }
        )
      }

      val processInstanceId = data.context.processInstanceId
      loadVariable(processInstanceId)(
        data
          .debug(s"Load - Wait for $variableName with ready value: $readyValue")
          .withRequestCount(0)
      )
    }

  end extension

  extension (sEvent: SMessageEvent)
    def event = sEvent.inOut

    def sendMessage()(using ScenarioData): ResultType =
      for {
        // default: try until it returns status 200
        given ScenarioData <- sEvent.optReadyVariable
          .map(_ => sEvent.loadVariable())
          .getOrElse(Right(summon[ScenarioData]))
        given ScenarioData <- sendMsg()
      } yield summon[ScenarioData]

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
          .debug(s"- Request body: $body")

        val response = request.send(backend)
        response.body
          .map { (body: String) =>
            summon[ScenarioData]
              .info(
                s"Message '${sEvent.name}' received"
              )
              .debug(s"- response body: $body")
          }
          .left
          .flatMap { _ =>
            tryOrFail(correlate(), sEvent)
          }
      end correlate
      correlate()(data.withRequestCount(0))
    }
  end extension // SReceiveMessageEvent

  extension (sEvent: SSignalEvent)
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

  extension (sEvent: STimerEvent)

    def getAndExecute()(using data: ScenarioData): ResultType =
      given ScenarioData = data.withTaskId(notSet)

      for {
        // default it waits until there is a job ready
        given ScenarioData <- sEvent.optReadyVariable
          .map(_ => sEvent.loadVariable())
          .getOrElse(Right(summon[ScenarioData]))
        given ScenarioData <- job()
        given ScenarioData <- executeTimer()
      } yield summon[ScenarioData]

    private def job()(using data: ScenarioData): ResultType = {
      def getJob(
          processInstanceId: Any
      )(data: ScenarioData): ResultType = {
        val uri =
          uri"${config.endpoint}/job?processInstanceId=$processInstanceId"
        val request = basicRequest
          .auth()
          .get(uri)

        given ScenarioData = data
          .info(
            s"TimerEvent '${sEvent.name}' get"
          )
          .debug(s"- URI: $uri")

        request
          .extractBody()
          .flatMap(body =>
            body.hcursor.downArray
              .downField("id")
              .as[String]
              .map { (jobId: String) =>
                summon[ScenarioData]
                  .withJobId(jobId)
                  .info(
                    s"TimerEvent '${sEvent.name}' ready"
                  )
                  .info(s"- jobId: $jobId")
                  .debug(s"- body: $body")
              }
              .left
              .flatMap { _ =>
                tryOrFail(getJob(processInstanceId), sEvent)
              }
          )
      }

      val processInstanceId = data.context.processInstanceId
      getJob(processInstanceId)(data.withRequestCount(0))
    }

    private def executeTimer()(using ScenarioData): ResultType =
      for {
        given ScenarioData <- execEvent()
      } yield summon[ScenarioData]

    private def execEvent()(using
        data: ScenarioData
    ): ResultType = {

      val uri = uri"${config.endpoint}/job/${data.context.jobId}/execute"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .post(uri)

      runRequest(request, s"Timer '${sEvent.name}' sent")((_, data) =>
        Right(data)
      )
    }
  end extension // SReceiveTimerEvent

end SEventExtensions
