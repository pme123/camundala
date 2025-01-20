package camundala
package worker

import camundala.domain.*
import camundala.bpmn.*
import camundala.worker.CamundalaWorkerError.*
import io.circe
import sttp.model.Uri.QuerySegment
import sttp.model.{Method, Uri}
import scala.reflect.ClassTag

trait WorkerHandler:
  def worker: Worker[?, ?, ?]
  def topic: String

  def applicationName: String
  def registerHandler( register: => Unit): Unit =
    val appPackageName = applicationName.replace("-", ".")
    val testMode       = sys.env.get("WORKER_TEST_MODE").contains("true") // did not work with lazy val
    if testMode || getClass.getName.startsWith(appPackageName)
    then
      register
      logger.info(s"Worker registered: $topic -> ${worker.getClass.getSimpleName}")
      logger.debug(prettyString(worker))
    else
      logger.info(
        s"Worker NOT registered: $topic -> ${worker.getClass.getSimpleName} (class starts not with $appPackageName)"
      )
    end if
  end registerHandler

  protected lazy val logger: WorkerLogger
end WorkerHandler

/** handler for Custom Validation (next to the automatic Validation of the In Object.
  *
  * For example if one of two optional variables must exist.
  *
  * Usage:
  * ```
  *  .withValidation(
  *    ValidationHandler(
  *      (in: In) => Right(in)
  *    )
  *  )
  * ```
  * or (with implicit conversion)
  * ```
  *  .withValidation(
  *      (in: In) => Right(in)
  *  )
  * ```
  * Default is no extra Validation.
  */
trait ValidationHandler[In <: Product: circe.Codec]:
  def validate(in: In): Either[ValidatorError, In]
end ValidationHandler

object ValidationHandler:
  def apply[
      In <: Product: InOutCodec
  ](funct: In => Either[ValidatorError, In]): ValidationHandler[In] =
    new ValidationHandler[In]:
      override def validate(in: In): Either[ValidatorError, In] =
        funct(in)
end ValidationHandler


type InitProcessFunction =
  EngineContext ?=> Either[InitProcessError, Map[String, Any]]

/** handler for Custom Process Initialisation. All the variables in the Result Map will be put on
  * the process.
  *
  * For example if you want to init process Variables to a certain value.
  *
  * Usage:
  * ```
  *  .withValidation(
  *    InitProcessHandler(
  *      (in: In) => {
  *       Right(
  *         Map("isCompany" -> true)
  *       ) // success
  *      }
  *    )
  *  )
  * ```
  * or (with implicit conversion)
  * ```
  *  .withValidation(
  *      (in: In) => {
  *       Right(
  *         Map("isCompany" -> true)
  *       ) // success
  *      }
  *  )
  * ```
  * Default is no Initialization.
  */
trait InitProcessHandler[
    In <: Product: InOutCodec
]:
  def init(input: In): InitProcessFunction
end InitProcessHandler
object InitProcessHandler:
  def apply[
      In <: Product: InOutCodec
  ](
      funct: In => InitProcessFunction,
      processLabels: ProcessLabels
  ): InitProcessHandler[In] =
    new InitProcessHandler[In]:
      override def init(in: In): InitProcessFunction =
        funct(in)
          .map:
            _ ++ processLabels.toMap

end InitProcessHandler

trait RunWorkHandler[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
]:
  type RunnerOutput =
    EngineRunContext ?=> Either[RunWorkError, Out]

  def runWork(inputObject: In): RunnerOutput
end RunWorkHandler

case class ServiceHandler[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    ServiceIn: InOutEncoder,
    ServiceOut: InOutDecoder: ClassTag
](
    httpMethod: Method,
    apiUri: In => Uri,
    querySegments: In => Seq[QuerySegmentOrParam],
    inputMapper: In => Option[ServiceIn],
    inputHeaders: In => Map[String, String],
    outputMapper: (ServiceResponse[ServiceOut], In) => Either[ServiceMappingError, Out],
    defaultServiceOutMock: MockedServiceResponse[ServiceOut],
    dynamicServiceOutMock: Option[In => MockedServiceResponse[ServiceOut]] = None,
    serviceInExample: ServiceIn
) extends RunWorkHandler[In, Out]:

  def runWork(
      inputObject: In
  ): RunnerOutput =
    val rRequest = runnableRequest(inputObject)
    for
      optWithServiceMock <- withServiceMock(rRequest, inputObject)
      output             <- handleMocking(optWithServiceMock, rRequest).getOrElse(
                              summon[EngineRunContext]
                                .sendRequest[ServiceIn, ServiceOut](rRequest)
                                .flatMap(out => outputMapper(out, inputObject))
                            )
    yield output
    end for
  end runWork

  private def runnableRequest(
      inputObject: In
  ): RunnableRequest[ServiceIn] =
    RunnableRequest(
      inputObject,
      httpMethod,
      apiUri(inputObject),
      querySegments(inputObject),
      inputMapper(inputObject),
      inputHeaders(inputObject)
    )

  private def withServiceMock(
      runnableRequest: RunnableRequest[ServiceIn],
      in: In
  )(using context: EngineRunContext): Either[ServiceError, Option[Out]] =
    (
      context.generalVariables.servicesMocked,
      context.generalVariables.outputServiceMock
    ) match
      case (_, Some(json)) =>
        (for
          mockedResponse <- decodeMock[MockedServiceResponse[ServiceOut]](json)
          out            <- handleServiceMock(mockedResponse, runnableRequest, in)
        yield out)
          .map(Some.apply)
      case (true, _)       =>
        handleServiceMock(
          dynamicServiceOutMock.map(_(in)).getOrElse(defaultServiceOutMock),
          runnableRequest,
          in
        )
          .map(Some.apply)
      case _               =>
        Right(None)

  end withServiceMock

  private def decodeMock[Out: InOutDecoder](
      json: Json
  ): Either[ServiceMockingError, Out] =
    decodeTo[Out](json.asJson.deepDropNullValues.toString).left
      .map(ex => ServiceMockingError(errorMsg = ex.causeMsg))
  end decodeMock

  private def handleMocking(
      optOutMock: Option[Out],
      runnableRequest: RunnableRequest[ServiceIn]
  )(using context: EngineRunContext): Option[Either[ServiceError, Out]] =
    optOutMock
      .map { mock =>
        context
          .getLogger(getClass)
          .info(s"""Mocked Service: ${niceClassName(this.getClass)}
                   |${requestMsg(runnableRequest)}
                   | - mockedResponse: ${mock.asJson.deepDropNullValues}
                   |""".stripMargin)
        mock
      }
      .map(m => Right(m))
  end handleMocking

  private def handleServiceMock(
      mockedResponse: MockedServiceResponse[ServiceOut],
      runnableRequest: RunnableRequest[ServiceIn],
      in: In
  ): Either[ServiceError, Out] =
    mockedResponse match
      case MockedServiceResponse(_, Right(body), headers) =>
        mapBodyOutput(body, headers, in)
      case MockedServiceResponse(status, Left(body), _)   =>
        Left(
          ServiceRequestError(
            status,
            serviceErrorMsg(
              status,
              s"Mocked Error: ${body.map(_.asJson.deepDropNullValues).getOrElse("-")}",
              runnableRequest
            )
          )
        )

  def mapBodyOutput(
      serviceOutput: ServiceOut,
      headers: Seq[Seq[String]],
      in: In
  ) =
    outputMapper(
      ServiceResponse(
        serviceOutput,
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      ),
      in
    )

end ServiceHandler

trait CustomHandler[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec
] extends RunWorkHandler[In, Out]:

end CustomHandler

object CustomHandler:
  def apply[
      In <: Product: InOutCodec,
      Out <: Product: InOutCodec
  ](funct: In => Either[CustomError, Out]): CustomHandler[In, Out] =
    new CustomHandler[In, Out]:
      override def runWork(inputObject: In): RunnerOutput =
        funct(inputObject)
end CustomHandler
