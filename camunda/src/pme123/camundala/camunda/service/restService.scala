package pme123.camundala.camunda.service

import java.util.concurrent.TimeUnit

import _root_.sttp.model.Uri._
import io.circe.Json
import pme123.camundala.app.sttpBackend.SttpTaskBackend
import pme123.camundala.camunda.service.restService.QueryParams.NoParams
import pme123.camundala.camunda.service.restService.Request.Auth.{BasicAuth, BearerAuth, DigestAuth, NoAuth}
import pme123.camundala.camunda.service.restService.Request.{Auth, Host}
import pme123.camundala.camunda.service.restService.RequestHeader.NoHeaders
import pme123.camundala.camunda.service.restService.RequestMethod.Get
import pme123.camundala.camunda.service.restService.RequestPath.NoPath
import pme123.camundala.camunda.service.restService.ResponseRead.{NoResponseRead, StringRead}
import pme123.camundala.model.bpmn.{CamundalaException, PathElem, PropKey}
import pme123.camundala.model.deploy.{Sensitive, Url, Username}
import sttp.{client => sttp, model => sttpModel}
import zio._
import zio.clock.Clock
import zio.duration._
import zio.logging.Logging

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

        implicit def sttpBackend: sttp.SttpBackend[Task, Nothing, sttp.NothingT] = backend

        (request: Request) => {

          lazy val sendRequest: Task[sttp.Response[Either[String, String]]] =
            sttp.basicRequest
              .headers(request.headers.toMap)
              .withAuth(request.host.auth)
              .withMethod(request)
              .withBody(request.body)
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

          def handleResponse(sttpRespEffect: Task[sttp.Response[Either[String, String]]]): Task[Response] =
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

          def handleResponseBody(status: sttpModel.StatusCode, sttpRespEffect: sttp.Response[Either[String, String]]): Task[Response] =
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

          handleResponse(sendRequest)
        }
    }

  private[service] def uri(request: Request) = {
    val uri = s"${
      request.host.url.value
    }${
      request.mappings.foldLeft(request.path.toString) {
        case (r, (k, v)) =>
          r.replace(k, v)
      }
    }"
    uri"$uri"
  }


  implicit class CRequestT(sttpRequest: sttp.RequestT[sttp.Empty, Either[String, String], Nothing]) {

    def withAuth(auth: Auth): sttp.RequestT[sttp.Empty, Either[String, String], Nothing] = auth match {
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

    def withMethod(request: Request): sttp.Request[Either[String, String], Nothing] = request.method match {
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

  implicit class CRequest(sttpRequest: sttp.Request[Either[String, String], Nothing]) {

    import RequestBody._

    def withBody(body: RequestBody): sttp.Request[Either[String, String], Nothing] = body match {
      case NoBody =>
        sttpRequest
      case StringBody(str) =>
        sttpRequest
          .body(str)
    }

    def withResponse(resp: ResponseRead): sttp.Request[Either[String, String], Nothing] = resp match {
      case NoResponseRead | StringRead =>
        sttpRequest
    }

  }

  case class Request(host: Host,
                     method: RequestMethod = Get,
                     path: RequestPath = NoPath,
                     queryParams: QueryParams = NoParams,
                     headers: RequestHeaders = NoHeaders,
                     body: RequestBody = RequestBody.NoBody,
                     responseRead: ResponseRead = StringRead,
                     handledErrors: Seq[Int] = Nil,
                     responseVariable: String = "jsonResult",
                     mappings: Map[String, String] = Map.empty
                    // maybeMocked: Option[Request => Response] = None
                    )

  object Request {

    case class Host(url: Url,
                    auth: Auth = NoAuth
                   )

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

  sealed trait RequestBody

  object RequestBody {

    case object NoBody extends RequestBody

    case class StringBody(str: String) extends RequestBody

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


  case class RestServiceException(msg: String,
                                  override val cause: Option[Throwable] = None)
    extends CamundalaException

}
