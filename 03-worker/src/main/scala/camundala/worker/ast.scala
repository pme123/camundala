package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.QuerySegmentOrParam.{Key, KeyValue, Value}
import sttp.model.Uri.QuerySegment
import sttp.model.{Method, Uri}

case class Workers(workers: Seq[Worker[?, ?, ?]])

sealed trait Worker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
]:

  def inOut: InOut[In, Out, ?]
  def topic: String
  lazy val in: In = inOut.in
  lazy val out: Out = inOut.out
  // handler
  def validationHandler: Option[ValidationHandler[In]] = None
  def initProcessHandler: Option[InitProcessHandler[In]] = None
  // no handler for mocking - all done from the InOut Object
  def runWorkHandler: Option[RunWorkHandler[In, Out]] = None
  // helper
  def variableNames: Seq[String] = in.productElementNames.toSeq

  def defaultMock(in: In)(using
      context: EngineRunContext
  ): MockerError | MockedOutput =
    MockedOutput(
      context.toEngineObject(out)
    )

  def executor(using context: EngineRunContext): WorkerExecutor[In, Out, T]
end Worker

case class InitWorker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
](
    inOut: InOut[In, Out, ?],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val initProcessHandler: Option[InitProcessHandler[In]] = None
) extends Worker[In, Out, InitWorker[In, Out]]:
  lazy val topic: String = inOut.id

  def validate(
      validator: ValidationHandler[In]
  ): InitWorker[In, Out] =
    copy(validationHandler = Some(validator))

  def initProcess(
      init: InitProcessHandler[In]
  ): InitWorker[In, Out] =
    copy(initProcessHandler = Some(init))

  def executor(using
      context: EngineRunContext
  ): WorkerExecutor[In, Out, InitWorker[In, Out]] =
    WorkerExecutor(this)

end InitWorker

case class CustomWorker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
](
    inOut: CustomTask[In, Out],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val runWorkHandler: Option[RunWorkHandler[In, Out]] = None
) extends Worker[In, Out, CustomWorker[In, Out]]:
  lazy val topic: String = inOut.topicName

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
    ServiceIn <: Product: Encoder,
    ServiceOut: Decoder
](
    inOut: ServiceTask[In, Out, ServiceOut],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val runWorkHandler: Option[ServiceHandler[In, Out, ServiceIn, ServiceOut]] = None
) extends Worker[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]]:
  lazy val topic: String = inOut.topicName

  def validate(
      handler: ValidationHandler[In]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(validationHandler = Some(handler))

  def runWork(
      handler: ServiceHandler[In, Out, ServiceIn, ServiceOut]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(runWorkHandler = Some(handler))

  override def defaultMock(in: In)(using
      context: EngineRunContext
  ): MockerError | MockedOutput =
    val mocked: Option[MockerError | MockedOutput] = // needed for Union Type
      runWorkHandler
        .map(handler =>
          handler
            .outputMapper(
              ServiceResponse(
                inOut.defaultServiceOutMock.unsafeBody,
                inOut.defaultServiceOutMock.headersAsMap
              ),
              in
            ) match
            case Right(out) => MockedOutput(context.toEngineObject(out))
            case Left(err) => MockerError(errorMsg = err.causeMsg)
        )
    mocked.getOrElse(
      MockerError(s"There is no ServiceRunner defined for Worker: $topic")
    )
  end defaultMock

  def executor(using
      context: EngineRunContext
  ): WorkerExecutor[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]] =
    WorkerExecutor(this)

end ServiceWorker

// ApiCreator that describes these variables
case class GeneralVariables(
    defaultMocked: Boolean = false,
    outputMockOpt: Option[Json] = None,
    outputServiceMockOpt: Option[Json] = None,
    mockedSubprocesses: Seq[String] = Seq.empty,
    outputVariables: Seq[String] = Seq.empty,
    handledErrors: Seq[String] = Seq.empty,
    regexHandledErrors: Seq[String] = Seq.empty,
    impersonateUserIdOpt: Option[String] = None
):
  def isMockedSubprocess(workerTopicName: String): Boolean =
    mockedSubprocesses.contains(workerTopicName)

end GeneralVariables

case class RunnableRequest[ServiceIn: Encoder](
    httpMethod: Method,
    apiUri: Uri,
    qSegments: Seq[QuerySegment],
    requestBodyOpt: Option[ServiceIn],
    headers: Map[String, String]
)

object RunnableRequest:

  def apply[In <: Product: InOutCodec, ServiceIn <: Product: Encoder](
      inputObject: In,
      requestHandler: ServiceHandler[In, ?, ServiceIn, ?]
  ): RunnableRequest[ServiceIn] =

    val valueMap: Map[String, String] =
      inputObject.productElementNames.toSeq
        .zip(inputObject.productIterator.toSeq)
        .collect {
          case k -> Some(v) => k -> s"$v"
          case k -> v if v != None => k -> s"$v"
        }
        .toMap

    val segments =
      requestHandler.querySegments
        .collect {
          case Value(v) => QuerySegment.Value(v)
          case KeyValue(k, v) => QuerySegment.KeyValue(k, v)
          case Key(k) if valueMap.contains(k) =>
            QuerySegment.KeyValue(k, valueMap(k))
        }

    new RunnableRequest[ServiceIn](
      requestHandler.httpMethod,
      requestHandler.apiUri(inputObject),
      segments,
      requestHandler.inputMapper(inputObject),
      requestHandler.inputHeaders(inputObject)
    )
  end apply
end RunnableRequest

case class ServiceResponse[ServiceOut](
    outputBody: ServiceOut,
    headers: Map[String, String]
)

enum QuerySegmentOrParam:
  case Key(key: String)
  case Value(value: String)
  case KeyValue(key: String, value: String)
end QuerySegmentOrParam
