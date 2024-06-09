package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.parser
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.Uri.QuerySegment
import sttp.model.{Header, Uri}

import scala.util.Try

trait RestApiClient:

  def sendRequest[
      ServiceIn: InOutEncoder, // body of service
      ServiceOut: InOutDecoder // output of service
  ](
      runnableRequest: RunnableRequest[ServiceIn]
  ): SendRequestType[ServiceOut] =
    try
      for
        reqWithOptBody <- requestWithOptBody(runnableRequest)
        req <- auth(reqWithOptBody)
        response = req.send(backend)
        statusCode = response.code
        body <- readBody(statusCode, response, req)
        headers = response.headers.map(h => h.name -> h.value).toMap
        out <- decodeResponse[ServiceOut](body)
      yield ServiceResponse(out, headers)
    catch
      case ex: Throwable =>
        ex.printStackTrace()
        Left(ServiceUnexpectedError(ex.getMessage))
  end sendRequest

  protected def readBody(
      statusCode: StatusCode,
      response: Response[Either[String, String]],
      request: Request[Either[String, String], Any]
  ): Either[ServiceRequestError, String] =
    response.body.left
      .map(body =>
        ServiceRequestError(
          statusCode.code,
          s"Non-2xx response with code $statusCode:\n$body\n\n${request.toCurl}"
        )
      )
  end readBody

  // no auth per default
  protected def auth(
      request: Request[Either[String, String], Any]
  )(using EngineRunContext): Either[ServiceAuthError, Request[Either[String, String], Any]] =
    Right(request)

  protected def decodeResponse[
      ServiceOut: InOutDecoder // output of service
  ](
      body: String
  ): Either[ServiceBadBodyError, ServiceOut] =
    if body.isBlank then
      NoOutput() match
        case o if o.isInstanceOf[ServiceOut] =>
          Right(NoOutput().asInstanceOf[ServiceOut])
        case _ =>
          Left(ServiceBadBodyError(
            "There is no body in the response and the ServiceOut is not NoOutput."
          ))
    else
      parser
        .decodeAccumulating[ServiceOut](body)
        .toEither
        .left
        .map(err => ServiceBadBodyError(s"Problem creating body from response.\n$err\nBODY: $body"))

  protected def requestWithOptBody[ServiceIn: InOutEncoder](
      runnableRequest: RunnableRequest[ServiceIn]
  ) =
    val request =
      requestMethod(
        runnableRequest.httpMethod,
        runnableRequest.apiUri,
        runnableRequest.qSegments,
        runnableRequest.headers
      )
    Try(runnableRequest.requestBodyOpt.map(b =>
      request.body(b.asJson.deepDropNullValues)
    ).getOrElse(request)).toEither.left
      .map(err => ServiceBadBodyError(errorMsg = s"Problem creating body for request.\n$err"))
  end requestWithOptBody

  private def requestMethod(
      httpMethod: Method,
      apiUri: Uri,
      qSegments: Seq[QuerySegment],
      headers: Map[String, String]
  ): Request[Either[String, String], Any] =
    basicRequest
      .copy(
        uri = apiUri.addQuerySegments(qSegments),
        headers = headers.toSeq.map { case k -> v => Header(k, v) },
        method = httpMethod
      )
  end requestMethod

end RestApiClient

object DefaultRestApiClient extends RestApiClient
