package camundala
package worker

import bpmn.*
import camundala.worker.CamundalaWorkerError.ValidatorError
import domain.*

import scala.reflect.ClassTag

private[worker] trait WorkerDsl :

  // needed that it can be called from CSubscriptionPostProcessor
  var workers: Workers = _

  def workers(body: => Worker[?, ?, ?]*): Unit =
    workers = Workers(body)
  end workers

  def process[
    In <: Product : CirceCodec: ClassTag,
    Out <: Product : CirceCodec,
  ](process: Process[In, Out])(using context: EngineContext): ProcessWorker[In, Out] =
    ProcessWorker(process)

  def service[
    In <: Product : CirceCodec: ClassTag,
    Out <: Product : CirceCodec,
    ServiceIn <: Product : Encoder,
    ServiceOut : Decoder,
  ](process: ServiceProcess[In, Out, ServiceIn, ServiceOut])(using context: EngineContext): ServiceWorker[In,Out,ServiceIn, ServiceOut] =
    ServiceWorker(process)

end WorkerDsl

