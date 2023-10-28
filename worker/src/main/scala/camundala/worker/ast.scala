package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import sttp.model.{Method, Uri}

case class Workers(workers: Seq[Worker[?, ?, ?]])

sealed trait Worker[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    T <: Worker[In, Out, ?]
]:

  def topic: String
  def inOut: InOut[In, Out, ?]
  lazy val in: In = inOut.in
  lazy val out: Out = inOut.out
  // handler
  def validationHandler: Option[ValidationHandler[In]] = None
  def initProcessHandler: Option[InitProcessHandler[In]] = None

  // helper
  def variableNames: Seq[String] = in.productElementNames.toSeq
  def defaultMock(using
      EngineContext
  ): Either[MockerError | MockedOutput, Option[Out]]

  def executor(using context: EngineContext): WorkerExecutor[In, Out, T]
  def workRunner: Option[WorkRunner[In, Out]]
end Worker

case class InitProcessWorker[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
](
    inOut: Process[In, Out],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    override val initProcessHandler: Option[InitProcessHandler[In]] = None
) extends Worker[In, Out, InitProcessWorker[In, Out]]:
  lazy val topic: String = inOut.processName

  def validation(
      validator: ValidationHandler[In]
  ): InitProcessWorker[In, Out] =
    copy(validationHandler = Some(validator))

  def initProcess(
      init: InitProcessHandler[In]
  ): InitProcessWorker[In, Out] =
    copy(initProcessHandler = Some(init))

  def defaultMock(using
      context: EngineContext
  ): Either[MockerError | MockedOutput, Option[Out]] = Left(
    MockedOutput(
      context.toEngineObject(out)
    )
  )

  def executor(using
      context: EngineContext
  ): WorkerExecutor[In, Out, InitProcessWorker[In, Out]] =
    WorkerExecutor(this)
  def workRunner: Option[WorkRunner[In, Out]] = None

end InitProcessWorker

case class ServiceWorker[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn <: Product: CirceCodec,
    ServiceOut: CirceCodec
](
    inOut: ServiceProcess[In, Out, ServiceIn, ServiceOut],
    override val validationHandler: Option[ValidationHandler[In]] = None,
    workRunner: Option[ServiceRunner[In, Out, ServiceIn, ServiceOut]] = None
)(using context: EngineContext)
    extends Worker[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]]:
  lazy val topic: String = inOut.serviceName

  def withValidation(
      validation: ValidationHandler[In]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(validationHandler = Some(validation))

  def withRequestHandler(
      requestHandler: RequestHandler[In, Out, ServiceIn, ServiceOut]
  ): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(workRunner = Some(ServiceRunner(this, requestHandler)))

  def defaultMock(using
      context: EngineContext
  ): Either[MockerError | MockedOutput, Option[Out]] =
    workRunner
      .map(_.requestHandler)
      .map(requestHandler =>
        requestHandler
          .outputMapper(
            RequestOutput(
              inOut.defaultServiceMock,
              requestHandler.defaultHeaders
            )
          )
          .left
          .map(err => MockerError(errorMsg = err.errorMsg))
      )
      .getOrElse(
        Left(
          MockerError(s"There is no ServiceRunner defined for Worker: $topic")
        )
      )

  def executor(using
      context: EngineContext
  ): WorkerExecutor[In, Out, ServiceWorker[In, Out, ServiceIn, ServiceOut]] =
    WorkerExecutor(this)

end ServiceWorker

// ApiCreator that describes these variables
case class GeneralVariables(
    servicesMocked: Boolean = false,
    outputMockOpt: Option[Json] = None,
    outputServiceMockOpt: Option[Json] = None,
    mockedSubprocesses: Seq[String] = Seq.empty,
    outputVariables: Seq[String] = Seq.empty,
    handledErrors: Seq[String] = Seq.empty,
    regexHandledErrors: Seq[String] = Seq.empty,
    impersonateUserIdOpt: Option[String] = None,
    serviceNameOpt: Option[String] = None
):
  def isMocked(workerTopicName: String): Boolean =
    mockedSubprocesses.contains(workerTopicName)

end GeneralVariables

case class RequestHandler[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn <: Product: Encoder,
    ServiceOut: Decoder
](
    httpMethod: Method,
    apiUri: Uri,
    defaultHeaders: Map[String, String] = Map.empty,
    sendRequest: RunnableRequest[ServiceIn] => Either[
      ServiceError,
      RequestOutput[ServiceOut]
    ],
    queryParamKeys: Seq[String | (String, String)] = Seq.empty,
    inputMapper: Option[In => ServiceIn] = None,
    outputMapper: RequestOutput[ServiceOut] => Either[MappingError, Option[
      Out
    ]] = (_: RequestOutput[ServiceOut]) => Right(None)
):

end RequestHandler

case class RunnableRequest[ServiceIn](
    httpMethod: Method,
    apiUri: Uri,
    queryParams: Seq[(String, Seq[String])],
    requestBodyOpt: Option[ServiceIn]
)

object RunnableRequest:

  def apply[In <: Product: CirceCodec, ServiceIn <: Product: CirceCodec](
      inputObject: In,
      requestInput: RequestHandler[In, ?, ServiceIn, ?]
  ): RunnableRequest[ServiceIn] =
    val defaultsMap = requestInput.queryParamKeys.map {
      case k -> v => k -> Some(v)
      case k => k -> None
    }.toMap

    val queryParams: Seq[(String, Seq[String])] =
      inputObject.productElementNames.toSeq
        .zip(inputObject.productIterator.toSeq)
        .collect {
          case k -> Some(value) if defaultsMap.contains(k) =>
            k -> Seq(s"$value")

          case k -> None if defaultsMap.get(k).flatten.isDefined =>
            k -> Seq(defaultsMap.get(k).flatten.get)

        }
    new RunnableRequest[ServiceIn](
      requestInput.httpMethod,
      requestInput.apiUri,
      queryParams,
      requestInput.inputMapper.map(m => m(inputObject))
    )
  end apply
end RunnableRequest

case class RequestOutput[ServiceOut](
    outputBody: ServiceOut,
    headers: Map[String, String]
)
