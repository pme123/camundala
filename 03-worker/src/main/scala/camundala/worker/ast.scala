package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import sttp.model.{Method, Uri}

import scala.reflect.ClassTag

case class Workers(workers: Seq[Worker[?, ?, ?]])

sealed trait Worker[
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec,
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
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec : ClassTag
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
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec:ClassTag
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
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec: ClassTag,
    ServiceIn <: Product: JsonEncoder,
    ServiceOut: JsonDecoder
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
      context: EngineRunContext,
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

case class RunnableRequest[ServiceIn: JsonEncoder](
    httpMethod: Method,
    apiUri: Uri,
    queryParams: Seq[(String, Seq[String])],
    requestBodyOpt: Option[ServiceIn],
    headers: Map[String,String],
)

object RunnableRequest:

  def apply[In <: Product: JsonCodec, ServiceIn <: Product : JsonEncoder](
      inputObject: In,
      requestHandler: ServiceHandler[In, ?, ServiceIn, ?]
  ): RunnableRequest[ServiceIn] =

    val defaultsMap = requestHandler.queryParamKeys.map {
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
      requestHandler.httpMethod,
      requestHandler.apiUri(inputObject),
      queryParams,
      requestHandler.inputMapper(inputObject),
      requestHandler.inputHeaders(inputObject)
    )
  end apply
end RunnableRequest

case class ServiceResponse[ServiceOut](
    outputBody: ServiceOut,
    headers: Map[String, String]
)

