package camundala
package simulation
package gatling

import api.*
import bpmn.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*

import scala.language.implicitConversions

trait SEventExtensions extends SimulationHelper:

  extension (sEvent: SReceiveMessageEvent)
    def event = sEvent.inOut

    def correlate(tenantId: Option[String]): Seq[ChainBuilder] =
      if (sEvent.optReadyVariable.nonEmpty) {
        val readyVariable = sEvent.readyVariable
        Seq(
          exec(_.set(readyVariable, null)),
          retryOrFail(
            loadVariable(sEvent.readyVariable),
            processReadyCondition(sEvent.readyVariable, sEvent.readyValue)
          ),
          correlateMsg(tenantId)
        )
      } else // default: try until it returns status 200
        Seq(
          exec(_.set("processState", null)),
          retryOrFail(
            correlateMsg(tenantId)
          )
        )

    private def correlateMsg(tenantId: Option[String]): ChainBuilder =
      val processInstanceId =
        if (sEvent.processInstanceId) Some("#{processInstanceId}")
        else None
      val tenant = // only set if there is no processInstanceId
        if (sEvent.processInstanceId) None
        else tenantId
      exec(
        http(s"Correlate Message '${event.messageName}' of '${event.id}'")
          .post(s"/message")
          .auth()
          .body(
            StringBody(
              CorrelateMessageIn(
                messageName = event.messageName,
                tenantId = tenant,
                processInstanceId = processInstanceId,
                processVariables = Some(event.camundaInMap)
              ).asJson.deepDropNullValues.toString
            )
          )
          .check(checkMaxCount)
          .check(status.saveAs("lastStatus"))
      ).exitHereIfFailed

  end extension

  extension (sEvent: SReceiveSignalEvent)
    def event = sEvent.inOut

    def sendSignal(): Seq[ChainBuilder] = {
      val signal = SendSignalIn(
        name = event.messageName,
        variables = Some(sEvent.camundaInMap)
      )
      Seq(
        exec(_.set(sEvent.readyVariable, null)),
        retryOrFail(
          loadVariable(sEvent.readyVariable),
          processReadyCondition(sEvent.readyVariable, sEvent.readyValue)
        ),
        exec(
          http(s"SendSignal '${event.messageName}' of '${event.id}'")
            .post(s"/signal")
            .auth()
            .body(
              StringBody(
                signal.asJson.deepDropNullValues.deepDropNullValues.toString
              )
            )
            .check(status.is(204))
        ).exitHereIfFailed
      )
    }

  end extension
