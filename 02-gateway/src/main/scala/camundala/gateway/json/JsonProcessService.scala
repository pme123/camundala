package camundala.gateway.json

import camundala.gateway.ProcessInfo
import io.circe.Json

trait JsonProcessService:
  def startProcess(
      processDefId: String,
      in: Json
  ): Json

  def startProcessAsync(
      processDefId: String,
      in: Json
  ): ProcessInfo

  def sendMessage(
      messageDefId: String,
      in: Json
  ): ProcessInfo

  def sendSignal(
      signalDefId: String,
      in: Json
  ): ProcessInfo
end JsonProcessService
