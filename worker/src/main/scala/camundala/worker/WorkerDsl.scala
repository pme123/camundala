package camundala
package worker

import bpmn.*
import domain.*

trait WorkerDsl :

  // needed that it can be called from CSubscriptionPostProcessor
  var workers: Workers = _

  def register(body: => Worker[?]*): Unit =
    workers = Workers(body)
  end register

  def worker[
    In <: Product : CirceCodec,
    Out <: Product : CirceCodec,
  ](process: Process[In, Out]): Worker[?] =
    ProcessWorker(process)
/*
  def worker[
    In <: Product : CirceCodec,
    Out <: Product : CirceCodec,
    ServiceIn <: Product : Encoder,
    ServiceOut : Decoder,
  ](process: ServiceProcess[In, Out, ServiceIn, ServiceOut]): Worker[?] =
    ServiceProcessWorker(process)

 */
end WorkerDsl

