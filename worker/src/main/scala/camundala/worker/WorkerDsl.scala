package camundala
package worker

import bpmn.*
import camundala.worker.CamundalaWorkerError.ValidatorError
import domain.*

trait WorkerDsl :

  // needed that it can be called from CSubscriptionPostProcessor
  var workers: Workers = _

  def register(body: => Worker[?, ?]*): Unit =
    workers = Workers(body)
  end register

  def process[
    In <: Product : CirceCodec,
    Out <: Product : CirceCodec,
  ](process: Process[In, Out]): ProcessWorker[In, Out] =
    ProcessWorker(process)

  def service[
    In <: Product : CirceCodec,
    Out <: Product : CirceCodec,
    ServiceIn <: Product : Encoder,
    ServiceOut : Decoder,
  ](process: ServiceProcess[In, Out, ServiceIn, ServiceOut]): ServiceWorker[In,Out,ServiceIn, ServiceOut] =
    ServiceWorker(process)

end WorkerDsl

