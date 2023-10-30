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
      In <: Product: CirceCodec
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
    In <: Product: CirceCodec
]:
  def init(input: In): Either[InitProcessError, Map[String, Any]]

object InitProcessHandler:
  def apply[
      In <: Product: CirceCodec
  ](funct: In => Either[InitProcessError, Map[String, Any]]): InitProcessHandler[In] =
    new InitProcessHandler[In] {
      override def init(in: In): Either[InitProcessError, Map[String, Any]] =
        funct(in)
    }

trait RunWorkHandler[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
]:
  type RunnerOutput =
    EngineContext ?=> Either[RunWorkError, Option[Out]]

  def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput

case class ServiceHandler[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn: Encoder,
    ServiceOut: Decoder
](
    httpMethod: Method,
    apiUri: Uri,
    queryParamKeys: Seq[String | (String, String)] = Seq.empty,
    // mocking out from outService and headers
    defaultHeaders: Map[String, String] = Map.empty,
    inputMapper: Option[In => ServiceIn] = None,
    outputMapper: RequestOutput[ServiceOut] => Either[ServiceMappingError, Option[Out]] =
      (_: RequestOutput[ServiceOut]) => Right(None),
) extends RunWorkHandler[In, Out]:

  def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput =
    val body = inputMapper.map(m => m(inputObject))
    val qParams = queryParams(inputObject)
    (for {
      apiUriWithParams <- pathWithParams(inputObject, apiUri)
      runnableRequest = RunnableRequest(httpMethod, apiUriWithParams, qParams, body)
      optWithServiceMock <- withServiceMock(runnableRequest)
      output <- handleMocking(optWithServiceMock, runnableRequest).getOrElse(
        summon[EngineContext].sendRequest(runnableRequest)
          .flatMap(outputMapper)
      )
    } yield output)

  private def withServiceMock(
      runnableRequest: RunnableRequest[ServiceIn]
  )(using context: EngineContext): Either[ServiceError, Option[Out]] =
    context.generalVariables.outputServiceMockOpt
      .map { json =>
        for {
          mockedResponse <- decodeMock[MockedServiceResponse[ServiceOut]](
            true,
            json
          )
          out <- handleServiceMock(
            mockedResponse,
            runnableRequest
          )
        } yield out

      }
      .getOrElse(Right(None))
      .left
      .map(e => ServiceUnexpectedError(s"There was an Error creating Service Mock: $e"))
  end withServiceMock

  private def handleMocking(
      optOutMock: Option[Out],
      runnableRequest: RunnableRequest[ServiceIn]
  ): Option[Either[ServiceError, Option[Out]]] =
    optOutMock
      .map { mock =>
        println(s"""Mocked Service: ${niceClassName(this.getClass)}
                   |${requestMsg(runnableRequest)}
                   | - mockedResponse: ${mock.asJson}
                   |""".stripMargin)
        mock
      }
      .map(m => Right(Some(m)))
  end handleMocking

  private def handleServiceMock(
      mockedResponse: Option[MockedServiceResponse[ServiceOut]],
      runnableRequest: RunnableRequest[ServiceIn]
  ): Either[ServiceError, Option[Out]] =
    mockedResponse
      .map {
        case MockedServiceResponse(_, Right(body), headers) =>
          mapBodyOutput(body, headers)
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
      .getOrElse(Right(None))
      .left
      .map(e => ServiceUnexpectedError(s"There was an Error handling Service Mock: $e"))

  def mapBodyOutput(
      serviceOutput: ServiceOut,
      headers: Seq[Seq[String]]
  ) =
    outputMapper(
      RequestOutput(
        serviceOutput,
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      )
    )

  val defaultsMap = queryParamKeys.map {
    case k -> v => k -> Some(v)
    case k => k -> None
  }.toMap

  private def pathWithParams(inputObject: In, apiUri: Uri): Either[ServiceBadPathError, Uri] =
    val params = inputObject.productElementNames
      .zip(inputObject.productIterator)
      .toMap

    val Uri(
      scheme,
      authority,
      pathSegments,
      querySegments,
      fragmentSegment
    ) = apiUri
    val newSegments = pathSegments.segments.map { ps =>
      val value: String = ps.v.toString
      value match
        case v if v.startsWith("{") && v.endsWith("}") =>
          params
            .get(v.drop(1).dropRight(1))
            .map(pathValue => Right(pathValue.toString))
            .getOrElse(Left(s"There is no Path Element '$v' in your In-Object: $inputObject"))
        case _ =>
          Right(value)
    }
    val (errors, successes) = newSegments.partition(_.isLeft)
    if (errors.isEmpty)
      Right(
        Uri(
          scheme,
          authority,
          Uri.PathSegments.absoluteOrEmptyS(successes.map(_.getOrElse("-")).toSeq),
          querySegments,
          fragmentSegment
        )
      )
    else
      Left(ServiceBadPathError(errors.map(_.swap.getOrElse("-")).mkString("\n")))

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
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
] extends RunWorkHandler[In, Out]:

end CustomHandler

object CustomHandler:
  def apply[
      In <: Product: CirceCodec,
      Out <: Product: CirceCodec
  ](funct: (In, Option[Out]) => Either[CustomError, Option[Out]]): CustomHandler[In, Out] =
    new CustomHandler[In, Out] {
      override def runWork(
          inputObject: In,
          optOutMock: Option[Out]
      ): RunnerOutput =
        funct(inputObject, optOutMock)
    }
