package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*

private[worker] trait WorkerDsl:

  // needed that it can be called from CSubscriptionPostProcessor
  var workers: Workers = _

  def workers(body: => Worker[?, ?, ?]*): Unit =
    workers = Workers(body)
  end workers

  def process[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec
  ](process: Process[In, Out])(using
      context: EngineContext
  ): ProcessWorker[In, Out] =
    ProcessWorker(process)

  def service[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec,
      ServiceIn <: Product: CirceCodec,
      ServiceOut: CirceCodec
  ](
      process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
      requestHandler: RequestHandler[In, Out, ServiceIn, ServiceOut]
  )(using
      context: EngineContext
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    ServiceWorker(process, requestHandler)
      .withWorkRunner(ServiceRunner(_))

end WorkerDsl
