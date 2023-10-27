package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*

import scala.reflect.ClassTag

case class Workers(workers: Seq[Worker[?,?,?]])

sealed trait Worker[In <: Product: CirceCodec : ClassTag, Out <: Product: CirceCodec, T <: Worker[In, Out, ?]]:

  def topic: String
  def in: In
  def out: Out
  def customValidator: Option[In => Either[ValidatorError, In]]
  def variableNames: Seq[String] = in.productElementNames.toSeq
  def defaultMock(using EngineContext): Either[MockerError | MockedOutput, Option[Out]]
  def variablesInit: Option[In => Either[InitializerError, Map[String, Any]]]

  def executer:WorkerExecutor[In, Out, T]

end Worker

case class ProcessWorker[
  In <: Product: CirceCodec : ClassTag,
  Out <: Product: CirceCodec,
](process: Process[In, Out],
  customValidator: Option[In => Either[ValidatorError, In]] = None,
  variablesInit: Option[In => Either[InitializerError, Map[String, Any]]] = None,
                         )(using context: EngineContext) extends Worker[In, Out, ProcessWorker[In, Out]]:
  lazy val topic: String = process.processName
  lazy val in: In = process.in
  lazy val out: Out = process.out

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ProcessWorker[In, Out] =
    copy(customValidator = Some(validator))

  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ProcessWorker[In, Out] =
      copy(variablesInit = Some(init))

  def defaultMock(using context:EngineContext): Either[MockerError | MockedOutput, Option[Out]] = Left(
    MockedOutput(
      context.toEngineObject(out)
  ))

  def executer: WorkerExecutor[In, Out, ProcessWorker[In, Out]] = WorkerExecutor(this)

end ProcessWorker

case class ServiceWorker[
  In <: Product: CirceCodec : ClassTag,
  Out <: Product: CirceCodec,
  ServiceIn <: Product: Encoder,
  ServiceOut : Decoder
](process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
  defaultHeaders:  Map[String, String] = Map.empty,
    // default is no output
  bodyOutputMapper: RequestOutput[ServiceOut] => Either[MappingError, Option[Out]] = (_:RequestOutput[ServiceOut]) => Right(None),
  customValidator: Option[In => Either[ValidatorError, In]] = None,
  variablesInit: Option[In => Either[InitializerError, Map[String, Any]]] = None,
                         )(using context: EngineContext) extends Worker[In, Out, ServiceWorker[In,Out,ServiceIn, ServiceOut]]:
  lazy val topic: String = process.serviceName
  lazy val in: In = process.in
  lazy val out: Out = process.out

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(customValidator = Some(validator))
  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(variablesInit = Some(init))
  def withDefaultHeaders(headers: Map[String, String]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(defaultHeaders = headers)
  def withBodyOutputMapper(mapper: RequestOutput[ServiceOut] => Either[MappingError, Option[Out]]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(bodyOutputMapper = mapper)

  def defaultMock(using context:EngineContext): Either[MockerError | MockedOutput, Option[Out]] =
    bodyOutputMapper(RequestOutput(process.defaultServiceMock, defaultHeaders)).left.map(
      err => MockerError(errorMsg = err.errorMsg)
    )

  def mapBodyOutput(
                               serviceOutput: ServiceOut,
                               headers: Seq[Seq[String]]
                             ) =
    bodyOutputMapper(
      RequestOutput(
        serviceOutput,
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      )
    )

  def executer: WorkerExecutor[In, Out, ServiceWorker[In,Out,ServiceIn, ServiceOut]]  = WorkerExecutor(this)

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

case class RequestOutput[ServiceOut](
                                      outputBody: ServiceOut,
                                      headers: Map[String, String]
                                    )