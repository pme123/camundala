package camundala.gateway

import camundala.domain.*
import sttp.tapir.Schema.annotations.description

trait ProcessService:
  @description("Starts a process synchronously and waits for completion")
  def startProcess[In <: Product : InOutEncoder, Out <: Product: InOutDecoder](
      @description("Process definition ID") processDefId: String, 
      @description("Input variables") in: In
  ): Out

  @description("Starts a process asynchronously")
  def startProcessAsync[In <: Product : InOutEncoder](
      @description("Process definition ID") processDefId: String, 
      @description("Input variables") in: In
  ): ProcessInfo

  @description("Sends a message to start a process or correlate with running instance")
  def sendMessage[In <: Product : InOutEncoder](
      @description("Message definition ID") messageDefId: String, 
      @description("Input variables") in: In
  ): ProcessInfo

  @description("Broadcasts a signal that can be caught by multiple process instances")
  def sendSignal[In <: Product : InOutEncoder](
      @description("Signal definition ID") signalDefId: String, 
      @description("Input variables") in: In
  ): ProcessInfo