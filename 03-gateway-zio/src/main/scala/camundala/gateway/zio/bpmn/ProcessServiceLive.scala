package camundala.gateway.zio.bpmn

import camundala.domain.*
import camundala.gateway.{ProcessInfo, ProcessService}
import camundala.gateway.zio.*
import zio.*
case class ProcessServiceLive() extends ProcessService:
  
  def startProcess[In <: Product: InOutEncoder, Out <: Product: InOutDecoder](
      processDefId: String, 
      in: In
  ): Out = ???

  def startProcessAsync[In <: Product : InOutEncoder](
      processDefId: String, 
      in: In
  ): ProcessInfo = ???

  def sendMessage[In <: Product : InOutEncoder](
      messageDefId: String, 
      in: In
  ): ProcessInfo = ???

  def sendSignal[In <: Product : InOutEncoder](
      signalDefId: String, 
      in: In
  ): ProcessInfo = ???

object ProcessServiceLive:
  val layer: ULayer[ProcessService] = 
    ZLayer.succeed(ProcessServiceLive())