package camundala.gateway.zio.json

import camundala.gateway.ProcessInfo
import camundala.gateway.json.JsonProcessService
import io.circe.Json
import zio.*

case class JsonProcessServiceLive() extends JsonProcessService:
  def startProcess(
      processDefId: String, 
      in: Json
  ): Json = ???
  
  def startProcessAsync(
      processDefId: String, 
      in: Json
  ): ProcessInfo = ???
  
  def sendMessage(
      messageDefId: String, 
      in: Json
  ): ProcessInfo = ???
  
  def sendSignal(
      signalDefId: String, 
      in: Json
  ): ProcessInfo = ???

object JsonProcessServiceLive:
  val layer: ULayer[JsonProcessService] = 
    ZLayer.succeed(JsonProcessServiceLive())