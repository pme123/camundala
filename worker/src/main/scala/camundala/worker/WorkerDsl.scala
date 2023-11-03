package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*

import scala.language.implicitConversions

trait WorkerDsl:

  def engineContext: EngineContext

  def getLogger(clazz: Class[?]): WorkerLogger

  // needed that it can be called from CSubscriptionPostProcessor
  def worker: Worker[?, ?, ?]

  lazy val topic: String = worker.topic

end WorkerDsl

trait InitProcessWorkerDsl[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
] extends WorkerDsl,
      ValidateDsl[In],
      InitProcessDsl[In]:

  var worker: InitProcessWorker[In, Out] = _

  def initProcess(process: Process[In, Out]): InitProcessWorker[In, Out] =
    InitProcessWorker(process)
      .validation(ValidationHandler(validate))
      .initProcess(InitProcessHandler(initProcess))

end InitProcessWorkerDsl

trait CustomWorkerDsl[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
] extends WorkerDsl,
      ValidateDsl[In],
      RunWorkDsl[In, Out]:

  var worker: CustomWorker[In, Out] = _

  def custom(
      service: CustomTask[In, Out]
  ): Unit =
    worker = CustomWorker(service)
      .validation(ValidationHandler(validate))
      .runWork(CustomHandler(runWork))
  end custom

end CustomWorkerDsl

trait ServiceWorkerDsl[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn <: Product: CirceCodec,
    ServiceOut: CirceCodec
] extends WorkerDsl,
      ValidateDsl[In],
      RunWorkDsl[In, Out]:

  var worker: ServiceWorker[In, Out, ServiceIn, ServiceOut] = _

  protected def service(
      service: ServiceTask[In, Out, ServiceIn, ServiceOut],
      method: Method
  ): Unit =
    worker = ServiceWorker(service)
      .validation(ValidationHandler(validate))
      .runWork(
        ServiceHandler(
          method,
          apiUri,
          queryParamKeys,
          defaultHeaders,
          inputMapper,
          outputMapper
        )
      )

  end service

  protected def serviceGET(
      task: ServiceTask[In, Out, ServiceIn, ServiceOut]
  ): Unit =
    service(task, Method.GET)
  end serviceGET

  protected def apiUri: Uri
  protected def queryParamKeys: Seq[String | (String, String)] = Seq.empty
  // mocking out from outService and headers
  protected def defaultHeaders: Map[String, String] = Map.empty
  protected def inputMapper(in: In): Option[ServiceIn] = None
  protected def outputMapper(
      out: RequestOutput[ServiceOut]
  ): Either[ServiceMappingError, Option[Out]] = Right(None)

end ServiceWorkerDsl

private trait ValidateDsl[
    In <: Product: CirceCodec
]:

  def validate(in: In): Either[ValidatorError, In] = Right(in)

end ValidateDsl

private trait InitProcessDsl[
    In <: Product: CirceCodec
]:

  def initProcess(in: In): Either[InitProcessError, Map[String, Any]] = Right(Map.empty)

end InitProcessDsl

private trait RunWorkDsl[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
]:

  def runWork(
      inputObject: In,
      optOutput: Option[Out]
  ): Either[CustomError, Option[Out]] = Right(optOutput)

end RunWorkDsl
