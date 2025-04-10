package camundala.gateway

import camundala.domain.*
import zio.*

trait ProcessService:
  def startProcess[In <: Product: InOutEncoder, Out <: Product: InOutDecoder](
      processDefId: String,
      in: In
  ): IO[GatewayError, Out]

  def startProcessAsync[In <: Product: InOutEncoder](
      processDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo]

  def sendMessage[In <: Product: InOutEncoder](
      messageDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo]

  def sendSignal[In <: Product: InOutEncoder](
      signalDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo]
end ProcessService
