package pme123.camundala.camunda.service

import java.util.concurrent.TimeUnit

import eu.timepit.refined.auto._
import io.circe.Json
import pme123.camundala.app.sttpBackend.SttpTaskBackend
import pme123.camundala.camunda.service.restService.QueryParams.NoParams
import pme123.camundala.camunda.service.restService.Request.Auth.{BasicAuth, BearerAuth, DigestAuth, NoAuth}
import pme123.camundala.camunda.service.restService.Request.{Auth, Host}
import pme123.camundala.camunda.service.restService.RequestBody.Part.{FilePart, StringPart}
import pme123.camundala.camunda.service.restService.RequestHeader.NoHeaders
import pme123.camundala.camunda.service.restService.RequestMethod.Get
import pme123.camundala.camunda.service.restService.RequestPath.NoPath
import pme123.camundala.camunda.service.restService.Response.{HandledError, NoContent, WithContent}
import pme123.camundala.camunda.service.restService.ResponseRead.{NoResponseRead, StringRead}
import pme123.camundala.model.bpmn._
import sttp.client.{Empty, NothingT, RequestT, SttpBackend, basicRequest, multipart, Request => SttpRequest, Response => SttpResponse}
import sttp.model.StatusCode
import sttp.model.Uri._
import zio._
import zio.clock.Clock
import zio.duration._
import zio.logging.Logging

import scala.annotation.nowarn

object restService {

  type RestService = Has[Service]

  trait Service {
    def call(request: Request): Task[Response]
  }

  def call(request: Request): RIO[RestService, Response] =
    ZIO.accessM(_.get.call(request))

  type RestServiceDeps = Has[SttpTaskBackend] with Logging with Clock

  lazy val live: RLayer[RestServiceDeps, RestService] =
    ZLayer.fromServices[SttpTaskBackend, logging.Logger[String], Clock.Service, Service] {
      (backend, log, clock) =>

        implicit def sttpBackend: SttpBackend[Task, Nothing, NothingT] = backend

        (request: Request) => {

          def mockRequest(mockData: MockData): Task[Response] = mockData match {
            case MockData(_, Json.Null) => UIO(NoContent)
            case MockData(status, body) if request.handledErrors.contains(status) => UIO(HandledError(status, Right(body.toString())))
            case MockData(status, body) if status < 400 => UIO(WithContent(status, body.toString()))
            case MockData(status, body) => Task.fail(RestServiceException(s"There was a Server Problem with Status $status\n$body"))
          }


          lazy val sendRequest: Task[SttpResponse[Either[String, String]]] =
            basicRequest
              .headers(request.headers.toMap)
              .withAuth(request.host.auth)
              .withMethod(request)
              .withBody(request)
              .withResponse(request.responseRead)
              .send()
              .tapError(error =>
                for {
                  t <- clock.currentTime(TimeUnit.SECONDS)
                  _ <- log.info(s"Failing attempt (${t % 100} s): ${error.getMessage}")
                } yield ()
              )
              .tap { r =>
                log.debug(s"Response with Status ${r.code}\n${r.body}")
              }
              .retry(Schedule.recurs(5) && Schedule.exponential(1.second))
              .provideLayer(ZLayer.succeed(clock))

          def handleResponse(sttpRespEffect: Task[SttpResponse[Either[String, String]]]): Task[Response] =
            sttpRespEffect
              .mapError(ex => RestServiceException(s"There was a Problem calling ${request.host.url}", Some(ex)))
              .flatMap { r =>
                val status = r.code
                if (status.isSuccess)
                  handleResponseBody(status, r)
                else if (request.handledErrors.contains(status.code))
                  ZIO.succeed(Response.HandledError(status.code, r.body))
                else
                  ZIO.fail(RestServiceException(s"Result for ${request.host.url} was not successful with Status ${status.code}"))
              }

          def handleResponseBody(status: StatusCode, sttpRespEffect: SttpResponse[Either[String, String]]): Task[Response] =
            request.responseRead match {
              case NoResponseRead =>
                ZIO.succeed(Response.NoContent)
              case StringRead =>
                UIO(sttpRespEffect)
                  .map(_.body)
                  .flatMap {
                    case Left(error: String) =>
                      ZIO.fail(RestServiceException(s"Could not Parse Response\n$error"))
                    case Right(value) =>
                      ZIO.succeed(Response.WithContent(status.code, value))
                  }
            }

          request.maybeMocked.map(mockRequest)
            .getOrElse(handleResponse(sendRequest))
        }
    }

  private[service] def uri(request: Request) = {
    val uri = s"${
      request.host.url
    }${
      request.path
    }${
      request.queryParams
    }"
    uri"${
      mapStr(uri, request.mappings)
    }"
  }

  private[service] def mapStr(str: String, mappings: Map[String, String]) =
    mappings.foldLeft(str) {
      case (r, (k, v)) =>
        r.replace(s"%$k", v)
    }

  implicit class CRequestT(sttpRequest: RequestT[Empty, Either[String, String], Nothing]) {

    def withAuth(auth: Auth): RequestT[Empty, Either[String, String], Nothing] = auth match {
      case NoAuth =>
        sttpRequest
      case BasicAuth(username, password) =>
        sttpRequest
          .auth.basic(username.value, password.value)
      case DigestAuth(username, password) =>
        sttpRequest
          .auth.digest(username.value, password.value)
      case BearerAuth(token) =>
        sttpRequest
          .auth.bearer(token.value)
    }

    import RequestMethod._

    def withMethod(request: Request): SttpRequest[Either[String, String], Nothing] = request.method match {
      case Get =>
        sttpRequest
          .get(uri(request))
      case Delete =>
        sttpRequest
          .delete(uri(request))
      case Head =>
        sttpRequest
          .head(uri(request))
      case Options =>
        sttpRequest
          .options(uri(request))
      case Put =>
        sttpRequest
          .put(uri(request))
      case Post =>
        sttpRequest
          .post(uri(request))
      case Patch =>
        sttpRequest
          .patch(uri(request))
    }

  }

  implicit class CRequest(sttpRequest: SttpRequest[Either[String, String], Nothing]) {

    import RequestBody._

    def withBody(request: Request): SttpRequest[Either[String, String], Nothing] =
      request.body.mapStr(request.mappings) match {
        case NoBody =>
          sttpRequest
        case StringBody(str) =>
          sttpRequest
            .body(str)
        case MultipartBody(parts) =>
          sttpRequest
            .multipartBody(
              parts.map {
                case StringPart(name, value) =>
                  multipart(name.value, value)
                case FilePart(name, fileName, data) =>
                  multipart(name.value, data).fileName(fileName.value)
              }.toSeq)

      }

    def withResponse(resp: ResponseRead): SttpRequest[Either[String, String], Nothing] = resp match {
      case NoResponseRead | StringRead =>
        sttpRequest
    }

  }

  /**
    *
    * @param host
    * @param method
    * @param path
    * @param queryParams
    * @param headers
    * @param body
    * @param responseRead
    * @param handledErrors
    * @param responseVariable
    * @param mappings You can have variables in your path, queryParams or body - like `%YourVariable`.
    *                 Your mappings: Map("YourVariable" -> "YourValue").
    *                 If a mapping is not provided - it throws an RestServiceException.
    * @param maybeMocked
    */
  case class Request(host: Host = Host.unknown,
                     method: RequestMethod = Get,
                     path: RequestPath = NoPath,
                     queryParams: QueryParams = NoParams,
                     headers: RequestHeaders = NoHeaders,
                     body: RequestBody = RequestBody.NoBody,
                     responseRead: ResponseRead = StringRead,
                     handledErrors: Seq[Int] = Nil,
                     responseVariable: String = "jsonResult",
                     mappings: Map[String, String] = Map.empty,
                     maybeMocked: Option[MockData] = None
                    )

  object Request {

    case class Host(url: Url,
                    auth: Auth = NoAuth
                   )

    object Host {
      val unknown: Host = Host("http://unknown")
    }

    sealed trait Auth

    object Auth {

      case object NoAuth
        extends Auth

      case class BasicAuth(username: Username,
                           password: Sensitive)
        extends Auth

      case class BearerAuth(token: Sensitive)
        extends Auth

      case class DigestAuth(username: Username,
                            password: Sensitive)
        extends Auth

    }

  }

  sealed trait RequestMethod

  object RequestMethod {

    case object Get extends RequestMethod

    case object Delete extends RequestMethod

    case object Post extends RequestMethod

    case object Put extends RequestMethod

    case object Patch extends RequestMethod

    case object Head extends RequestMethod

    case object Options extends RequestMethod

  }

  sealed trait RequestPath

  object RequestPath {

    case object NoPath extends RequestPath {
      override def toString: String = ""
    }

    case class Path(elems: PathElem*) extends RequestPath {
      override def toString: String = elems.mkString("/", "/", "")
    }

  }

  sealed trait RequestBody {
    @nowarn("cat=unused-params")
    def mapStr(mappings: Map[String, String]): RequestBody = this
  }

  object RequestBody {

    case object NoBody extends RequestBody

    case class StringBody(str: String) extends RequestBody {
      override def mapStr(mappings: Map[String, String]): RequestBody =
        copy(str = restService.mapStr(str, mappings))

    }

    case class MultipartBody(parts: Set[Part]) extends RequestBody {

      override def mapStr(mappings: Map[String, String]): RequestBody =
        copy(parts = parts.map(_.mapStr(mappings)))

    }

    sealed trait Part {
      @nowarn("cat=unused-params")
      def mapStr(mappings: Map[String, String]): Part = this

    }

    object Part {

      case class StringPart(name: PropKey, value: String) extends Part {
        override def mapStr(mappings: Map[String, String]): StringPart =
          copy(value = restService.mapStr(value, mappings))
      }

      case class FilePart(name: FilePath, fileName: FilePath, data: String) extends Part

    }

  }

  sealed trait QueryParams

  object QueryParams {

    case object NoParams extends QueryParams {
      override def toString: String = ""
    }

    case class Params(elems: (PropKey, String)*) extends QueryParams {
      override def toString: String =
        elems.map { case (k, v) => s"$k=$v" }.mkString("?", "&", "")

    }

  }

  sealed trait RequestHeaders {
    def toMap: Map[String, String]

  }

  object RequestHeader {

    case object NoHeaders extends RequestHeaders {
      def toMap: Map[String, String] = Map.empty
    }

    case class Headers(elems: (PropKey, String)*) extends RequestHeaders {
      def toMap: Map[String, String] =
        elems.map { case (k, v) => k.value -> v }.toMap
    }

  }

  sealed trait ResponseRead

  object ResponseRead {

    case object NoResponseRead extends ResponseRead

    case object StringRead extends ResponseRead

  }

  case class ExpectedDataMock(
                               status: Int = 200,
                               maybeBody: Option[Json] = None
                             )

  sealed trait Response

  object Response {

    case object NoContent extends Response

    case class WithContent(status: Int,
                           body: String
                          ) extends Response

    case class HandledError(status: Int,
                            body: Either[String, String]
                           ) extends Response

  }

  case class MockData(respStatus: Int = 200, respBody: Json = Json.Null)

  case class RestServiceException(msg: String,
                                  override val cause: Option[Throwable] = None)
    extends CamundalaException

}
