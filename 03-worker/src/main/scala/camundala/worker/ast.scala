package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.QuerySegmentOrParam.{Key, KeyValue, Value}
import sttp.model.Uri.QuerySegment
import sttp.model.{Method, Uri}
import zio.{IO, ZIO}

case class Workers(workers: Seq[Worker[?, ?, ?]])

sealed trait Worker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
]:

  def inOutExample: InOut[In, Out, ?]
  def topic: String

  lazy val inVariableNames = inOutExample.inVariableNames
  lazy val in: In          = inOutExample.in
  lazy val out: Out        = inOutExample.out

  // handler
  def validationHandler: Option[ValidationHandler[In]]   = None
  def initProcessHandler: Option[InitProcessHandler[In]] = None
  // no handler for mocking - all done from the InOut Object
  def runWorkHandler: Option[RunWorkHandler[In, Out]]    = None

  // helper
  lazy val variableNames: Seq[String] =
    inVariableNames ++
      inConfigVariableNames

  lazy val inConfigVariableNames: Seq[String] =
    in match
      case i: WithConfig[?] =>
        i.defaultConfig.productElementNames.toSeq
      case _                => Seq.empty

  def defaultMock(in: In): IO[MockerError, Out] =
    ZIO.succeed(
      inOutExample match
        case e: ProcessOrExternalTask[In, Out, ?] =>
          e.dynamicOutMock.map(_(in)).getOrElse(out)
        case _                                    => out
    )
  end defaultMock

  def executor(using context: EngineRunContext): WorkerExecutor[In, Out, T]
end Worker

case class InitWorker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    InitIn <: Product: InOutCodec
](
    inOutExample: InOut[In, Out, ?],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val initProcessHandler: Option[InitProcessHandler[In]] = None
) extends Worker[In, Out, InitWorker[In, Out, InitIn]]:
  lazy val topic: String = inOutExample.id

  def validate(
      validator: ValidationHandler[In]
  ): InitWorker[In, Out, InitIn] =
    copy(validationHandler = Some(validator))

  def initProcess(
      init: InitProcessHandler[In]
  ): InitWorker[In, Out, InitIn] =
    copy(initProcessHandler = Some(init))

  def executor(using
      context: EngineRunContext
  ): WorkerExecutor[In, Out, InitWorker[In, Out, InitIn]] =
    WorkerExecutor(this)

end InitWorker

case class CustomWorker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
](
    inOutExample: CustomTask[In, Out],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val runWorkHandler: Option[RunWorkHandler[In, Out]] = None
) extends Worker[In, Out, CustomWorker[In, Out]]:
  lazy val topic: String = inOutExample.topicName

  def validate(
      validator: ValidationHandler[In]
  ): CustomWorker[In, Out] =
    copy(validationHandler = Some(validator))

  def runWork(
      serviceHandler: CustomHandler[In, Out]
  ): CustomWorker[In, Out] =
    copy(runWorkHandler = Some(serviceHandler))

  def executor(using
      context: EngineRunContext
  ): WorkerExecutor[In, Out, CustomWorker[In, Out]] =
    WorkerExecutor(this)

end CustomWorker

case class ServiceWorker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    ServiceIn: InOutEncoder,
    ServiceOut: InOutDecoder
](
    inOutExample: ServiceTask[In, Out, ServiceIn, ServiceOut],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val runWorkHandler: Option[ServiceHandler[In, Out, ServiceIn, ServiceOut]] = None
) extends Worker[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]]:
  lazy val topic: String = inOutExample.topicName

  def validate(
      handler: ValidationHandler[In]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(validationHandler = Some(handler))

  def runWork(
      handler: ServiceHandler[In, Out, ServiceIn, ServiceOut]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(runWorkHandler = Some(handler))

  override def defaultMock(in: In): IO[MockerError, Out] =
    runWorkHandler
      .map(handler =>
        ZIO.fromEither(
          handler
            .outputMapper(
              inOutExample.dynamicServiceOutMock
                .map:
                  _(in).toServiceResponse
                .getOrElse:
                  inOutExample.defaultServiceOutMock.toServiceResponse
              ,
              in
            )
        ).mapError: error =>
          MockerError(s"Error mapping ServiceResponse to Out: $error")
      )
      .getOrElse(
        ZIO.fail(MockerError(s"There is no ServiceRunner defined for Worker: $topic"))
      )
  end defaultMock

  def executor(using
      context: EngineRunContext
  ): WorkerExecutor[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]] =
    WorkerExecutor(this)

end ServiceWorker

case class RunnableRequest[ServiceIn: InOutEncoder](
    httpMethod: Method,
    apiUri: Uri,
    qSegments: Seq[QuerySegment],
    requestBodyOpt: Option[ServiceIn],
    headers: Map[String, String]
)

object RunnableRequest:

  def apply[In <: Product: InOutCodec, ServiceIn: InOutEncoder](
      inputObject: In,
      httpMethod: Method,
      apiUri: Uri,
      querySegments: Seq[QuerySegmentOrParam],
      optRequestBody: Option[ServiceIn],
      headers: Map[String, String]
  ): RunnableRequest[ServiceIn] =

    val valueMap: Map[String, String] =
      inputObject.productElementNames.toSeq
        .zip(inputObject.productIterator.toSeq)
        .collect {
          case k -> Some(v)        => k -> s"$v"
          case k -> v if v != None => k -> s"$v"
        }
        .toMap

    val segments =
      querySegments
        .collect {
          case Value(v)                       => QuerySegment.Value(v)
          case KeyValue(k, v)                 => QuerySegment.KeyValue(k, v)
          case Key(k) if valueMap.contains(k) =>
            QuerySegment.KeyValue(k, valueMap(k))
        }

    new RunnableRequest[ServiceIn](
      httpMethod,
      apiUri,
      segments,
      optRequestBody,
      headers
    )
  end apply
end RunnableRequest

case class ServiceResponse[ServiceOut](
    outputBody: ServiceOut,
    headers: Map[String, String] = Map.empty
)

extension [ServiceOut](mocked: MockedServiceResponse[ServiceOut])
  /** Simplifies testing, as there is already a successful service mock example.
    */
  def toServiceResponse: ServiceResponse[ServiceOut] =
    ServiceResponse(
      mocked.unsafeBody,
      mocked.headersAsMap
    )
end extension

enum QuerySegmentOrParam:
  case Key(key: String)
  case Value(value: String)
  case KeyValue(key: String, value: String)
end QuerySegmentOrParam
