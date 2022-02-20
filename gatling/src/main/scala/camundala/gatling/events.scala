package camundala.gatling

import camundala.api.*
import camundala.bpmn.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*

import scala.annotation.targetName

trait EventExtensions:
  extension [
    In <: Product: Encoder: Decoder: Schema
  ](event: ReceiveMessageEvent[In])
  
    @targetName("correlateReceiveMessageEvent")
    def correlate(): WithConfig[ChainBuilder] =
      correlateMsg(None)
  
    @targetName("correlateReceiveMessageEvent")
    def correlate(
                   readyVariable: String,
                   readyValue: Any,
                   tenantId: Option[String]
                 ): WithConfig[Seq[ChainBuilder]] = {
      Seq(
        exec(_.set(readyVariable, null)),
        retryOrFail(
          loadVariable(readyVariable),
          processReadyCondition(readyVariable, readyValue)
        ),
        correlateMsg(tenantId)
      )
    }
  
    private def correlateMsg(tenantId: Option[String] = None): WithConfig[ChainBuilder] =
      val processInstanceId = tenantId match
        case Some(_) => None
        case _ => Some("#{processInstanceId}")
      exec(
        http(s"Correlate Message '${event.messageName}' of '${event.id}'")
          .post(s"/message")
          .auth()
          .body(
            StringBody(
              CorrelateMessageIn(
                messageName = event.messageName,
                tenantId = tenantId,
                processInstanceId = processInstanceId,
                processVariables = Some(CamundaVariable.toCamunda(event.in))
              ).asJson.deepDropNullValues.toString
            )
          )
          .check(checkMaxCount)
          .check(status.saveAs("lastStatus"))
      ).exitHereIfFailed
  
  end extension
  
  extension [
    In <: Product: Encoder: Decoder: Schema
  ](event: ReceiveSignalEvent[In])
  
    def sendSignal(
                    readyVariable: String,
                    readyValue: Any = true
                  ): WithConfig[Seq[ChainBuilder]] =
      Seq(
        exec(_.set(readyVariable, null)),
        retryOrFail(
          loadVariable(readyVariable),
          processReadyCondition(readyVariable, readyValue)
        ),
        exec(
          http(s"SendSignal '${event.messageName}' of '${event.id}'")
            .post(s"/signal")
            .auth()
            .body(
              StringBody(
                SendSignalIn(
                  name = event.messageName,
                  variables = Some(CamundaVariable.toCamunda(event.in))
                ).asJson.deepDropNullValues.deepDropNullValues.toString
              )
            )
            .check(status.is(204))
        ).exitHereIfFailed
      )
  
  end extension
  
