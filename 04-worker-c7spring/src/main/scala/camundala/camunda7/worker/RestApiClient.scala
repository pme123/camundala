package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.parser
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.{Header, Uri}

import scala.util.Try

trait RestApiClient:

  def sendRequest[
      ServiceIn: Encoder, // body of service
      ServiceOut: Decoder // output of service
  ](
      runnableRequest: RunnableRequest[ServiceIn]
  ): Either[ServiceError, ServiceResponse[ServiceOut]] =
    try {
      for {
        reqWithOptBody <- requestWithOptBody(runnableRequest)
        req <- auth(reqWithOptBody)
        response = req.send(backend)
        statusCode = response.code
        body <- readBody(statusCode, response, req)
        headers = response.headers.map(h => h.name -> h.value).toMap
        out <- decodeResponse[ServiceOut](body)
      } yield ServiceResponse(out, headers)
    } catch {
      case ex: Throwable =>
        ex.printStackTrace()
        Left(ServiceUnexpectedError(ex.getMessage))
    }
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
  ): Either[ServiceAuthError, Request[Either[String, String], Any]] =
    Right(request)

  protected def decodeResponse[
      ServiceOut: Decoder // output of service
  ](
      body: String
  ): Either[ServiceBadBodyError, ServiceOut] =
    parser
      .decodeAccumulating[ServiceOut](body)
      .toEither
      .left
      .map(err => ServiceBadBodyError(s"Problem creating body from response.\n$err"))

  protected def requestWithOptBody[ServiceIn: Encoder](
      runnableRequest: RunnableRequest[ServiceIn]
  ) =
    val request =
      requestMethod(runnableRequest.httpMethod, runnableRequest.apiUri, runnableRequest.queryParams, runnableRequest.headers)
    Try(runnableRequest.requestBodyOpt.map(b => request.body(b)).getOrElse(request)).toEither.left
      .map(err => ServiceBadBodyError(errorMsg = s"Problem creating body for request.\n$err"))

  private def requestMethod(
                             httpMethod: Method,
                             apiUri: Uri,
                             qParams: Seq[(String, Seq[String])],
                             headers: Map[String, String]
                           ): Request[Either[String, String], Any] =
    basicRequest
      .copy(uri = apiUri.params(QueryParams(qParams)), headers = headers.toSeq.map{case k -> v => Header(k,v)}, method = httpMethod)
  end requestMethod

end RestApiClient

object DefaultRestApiClient extends RestApiClient
