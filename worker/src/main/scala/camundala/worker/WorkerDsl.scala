package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.{CustomError, InitProcessError, ValidatorError}

import scala.language.implicitConversions

private[worker] trait WorkerDsl:

  // needed that it can be called from CSubscriptionPostProcessor
  var workers: Workers = _

  def workers(body: => Worker[?, ?, ?]*): Unit =
    workers = Workers(body)
  end workers

  def initProcess[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec
  ](process: Process[In, Out])(using
      context: EngineContext
  ): InitProcessWorker[In, Out] =
    InitProcessWorker(process)

  def service[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec,
      ServiceIn <: Product: CirceCodec,
      ServiceOut: CirceCodec
  ](
      service: ServiceTask[In, Out, ServiceIn, ServiceOut]
  )(using
      context: EngineContext
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    ServiceWorker(service)

  def custom[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec
  ](
      service: CustomTask[In, Out]
  )(using EngineContext): CustomWorker[In, Out] =
    CustomWorker(service)

  // implicit Converters
  implicit def convert[In <: Product: CirceCodec](
      funct: In => Either[ValidatorError, In]
  ): ValidationHandler[In] =
    ValidationHandler(funct)

  implicit def init[In <: Product: CirceCodec](
      funct: In => Either[InitProcessError, Map[String, Any]]
  ): InitProcessHandler[In] =
    InitProcessHandler(funct)

  implicit def customx[In <: Product : CirceCodec, Out <: Product : CirceCodec ](
                                                 funct: (In, Option[Out]) => Either[CustomError, Option[Out]]
                                               ): CustomHandler[In, Out] =
    CustomHandler(funct)

end WorkerDsl
