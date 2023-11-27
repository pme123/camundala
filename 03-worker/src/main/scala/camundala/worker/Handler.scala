package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import io.circe
import sttp.model.{Method, Uri}

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

object ValidationHandler:
  def apply[
      In <: Product: JsonCodec
  ](funct: In => Either[ValidatorError, In]): ValidationHandler[In] =
    new ValidationHandler[In] {
      override def validate(in: In): Either[ValidatorError, In] =
        funct(in)
    }

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
    In <: Product: JsonCodec
]:
  def init(input: In): Either[InitProcessError, Map[String, Any]]

object InitProcessHandler:
  def apply[
      In <: Product: JsonCodec
  ](funct: In => Either[InitProcessError, Map[String, Any]]): InitProcessHandler[In] =
    new InitProcessHandler[In] {
      override def init(in: In): Either[InitProcessError, Map[String, Any]] =
        funct(in)
    }

trait RunWorkHandler[
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec
]:
  type RunnerOutput =
    EngineRunContext ?=> Either[RunWorkError, Out]

  def runWork(inputObject: In): RunnerOutput

case class ServiceHandler[
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec,
    ServiceIn <: Product: Encoder,
    ServiceOut: Decoder
](
    httpMethod: Method,
    apiUri: In => Uri,
    queryParamKeys: Seq[String | (String, String)],
    inputMapper: In => Option[ServiceIn],
    inputHeaders: In => Map[String, String],
    outputMapper: (ServiceResponse[ServiceOut], In) => Either[ServiceMappingError, Out],
    defaultServiceOutMock: MockedServiceResponse[ServiceOut]
) extends RunWorkHandler[In, Out]:

  def runWork(
      inputObject: In
  ): RunnerOutput =
    val rRequest = runnableRequest(inputObject)
    for {
      optWithServiceMock <- withServiceMock(rRequest, inputObject)
      output <- handleMocking(optWithServiceMock, rRequest).getOrElse(
        summon[EngineRunContext]
          .sendRequest[ServiceIn, ServiceOut](rRequest)
          .flatMap(out => outputMapper(out, inputObject))
      )
    } yield output

  private def runnableRequest(
      inputObject: In
  ): RunnableRequest[ServiceIn] =
    val body = inputMapper(inputObject)
    val uri = apiUri(inputObject)
    val qParams = queryParams(inputObject)
    val headers = inputHeaders(inputObject)
    RunnableRequest(httpMethod, uri, qParams, body, headers)

  private def withServiceMock(
      runnableRequest: RunnableRequest[ServiceIn],
      in: In
  )(using context: EngineRunContext): Either[ServiceError, Option[Out]] =
    (
      context.generalVariables.defaultMocked,
      context.generalVariables.outputServiceMockOpt
    ) match
      case (_, Some(json)) =>
        (for {
          mockedResponse <- decodeMock[MockedServiceResponse[ServiceOut]](json)
          out <- handleServiceMock(mockedResponse, runnableRequest, in)
        } yield out)
          .map(Some.apply)
      case (true, _) =>
        handleServiceMock(defaultServiceOutMock, runnableRequest, in)
          .map(Some.apply)
      case _ =>
        Right(None)

  end withServiceMock

  private def decodeMock[Out: Decoder](
      json: Json
  ): Either[ServiceMockingError, Out] =
    decodeTo[Out](json.asJson.toString).left
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
                   | - mockedResponse: ${mock.asJson}
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
    mockedResponse match {
      case MockedServiceResponse(_, Right(body), headers) =>
        mapBodyOutput(body, headers, in)
      case MockedServiceResponse(status, Left(body), _) =>
        Left(
          ServiceRequestError(
            status,
            serviceErrorMsg(
              status,
              s"Mocked Error: ${body.asJson}",
              runnableRequest
            )
          )
        )
    }

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

  val defaultsMap = queryParamKeys.map {
    case k -> v => k -> Some(v)
    case k => k -> None
  }.toMap

  private def queryParams(inputObject: In): Seq[(String, Seq[String])] =
    inputObject.productElementNames.toSeq
      .zip(inputObject.productIterator.toSeq)
      .collect {
        case k -> Some(value) if defaultsMap.contains(k) =>
          k -> Seq(s"$value")

        case k -> None if defaultsMap.get(k).flatten.isDefined =>
          k -> Seq(defaultsMap.get(k).flatten.get)

      }
end ServiceHandler

trait CustomHandler[
    In <: Product: JsonCodec,
    Out <: Product: JsonCodec
] extends RunWorkHandler[In, Out]:

end CustomHandler

object CustomHandler:
  def apply[
      In <: Product: JsonCodec,
      Out <: Product: JsonCodec
  ](funct: In => Either[CustomError, Out]): CustomHandler[In, Out] =
    new CustomHandler[In, Out] {
      override def runWork(inputObject: In): RunnerOutput =
        funct(inputObject)
    }
