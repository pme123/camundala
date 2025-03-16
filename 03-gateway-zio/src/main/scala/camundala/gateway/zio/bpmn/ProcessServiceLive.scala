package camundala.gateway.zio.bpmn

import camundala.domain.*
import camundala.gateway.{ProcessInfo, ProcessService}
import camundala.gateway.json.JsonProcessService
import io.circe.syntax.*
import zio.*

case class ProcessServiceLive(jsonService: JsonProcessService) extends ProcessService:
  def startProcess[In <: Product: InOutEncoder, Out <: Product: InOutDecoder](
      processDefId: String, 
      in: In
  ): Out = 
    val jsonIn = in.asJson
    val jsonOut = jsonService.startProcess(processDefId, jsonIn)
    jsonOut.as[Out].fold(throw _, identity)

  def startProcessAsync[In <: Product: InOutEncoder](
      processDefId: String, 
      in: In
  ): ProcessInfo = 
    val jsonIn = in.asJson
    jsonService.startProcessAsync(processDefId, jsonIn)

  def sendMessage[In <: Product: InOutEncoder](
      messageDefId: String, 
      in: In
  ): ProcessInfo = 
    val jsonIn = in.asJson
    jsonService.sendMessage(messageDefId, jsonIn)

  def sendSignal[In <: Product: InOutEncoder](
      signalDefId: String, 
      in: In
  ): ProcessInfo = 
    val jsonIn = in.asJson
    jsonService.sendSignal(signalDefId, jsonIn)

object ProcessServiceLive:
  val layer: URLayer[JsonProcessService, ProcessService] = 
    ZLayer.fromFunction(ProcessServiceLive(_))