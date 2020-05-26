package pme123.camundala.camunda

import io.circe.{Decoder, Encoder}
import io.circe.refined._
import io.circe.parser.decode
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import pme123.camundala.camunda.httpDeployClient.HttpDeployClientException
import pme123.camundala.camunda.service.restService.Request.{Auth, Host}
import pme123.camundala.camunda.service.restService.RequestBody.Part
import pme123.camundala.camunda.service.restService._
import pme123.camundala.camunda.xml.{ValidateWarning, ValidateWarnings}
import pme123.camundala.model.bpmn.Sensitive
import zio.ZIO

trait JsonEnDecoders {
  // model
  implicit val sensitiveEncoder: Encoder[Sensitive] = deriveEncoder[Sensitive]
  implicit val sensitiveDecoder: Decoder[Sensitive] = deriveDecoder[Sensitive]

  // camunda
  implicit val authEncoder: Encoder[Auth] = deriveEncoder[Auth]
  implicit val authDecoder: Decoder[Auth] = deriveDecoder[Auth]
  implicit val hostEncoder: Encoder[Host] = deriveEncoder[Host]
  implicit val hostDecoder: Decoder[Host] = deriveDecoder[Host]

  implicit val methodEncoder: Encoder[RequestMethod] = deriveEncoder[RequestMethod]
  implicit val methodDecoder: Decoder[RequestMethod] = deriveDecoder[RequestMethod]
  implicit val requestPathEncoder: Encoder[RequestPath] = deriveEncoder[RequestPath]
  implicit val requestPathDecoder: Decoder[RequestPath] = deriveDecoder[RequestPath]
  implicit val queryParamsEncoder: Encoder[QueryParams] = deriveEncoder[QueryParams]
  implicit val queryParamsDecoder: Decoder[QueryParams] = deriveDecoder[QueryParams]
  implicit val requestHeadersEncoder: Encoder[RequestHeaders] = deriveEncoder[RequestHeaders]
  implicit val requestHeadersDecoder: Decoder[RequestHeaders] = deriveDecoder[RequestHeaders]
  implicit val partEncoder: Encoder[Part] = deriveEncoder[Part]
  implicit val partDecoder: Decoder[Part] = deriveDecoder[Part]
  implicit val requestBodyEncoder: Encoder[RequestBody] = deriveEncoder[RequestBody]
  implicit val requestBodyDecoder: Decoder[RequestBody] = deriveDecoder[RequestBody]
  implicit val responseReadEncoder: Encoder[ResponseRead] = deriveEncoder[ResponseRead]
  implicit val responseReadDecoder: Decoder[ResponseRead] = deriveDecoder[ResponseRead]
  implicit val requestEncoder: Encoder[Request] = deriveEncoder[Request]
  implicit val requestDecoder: Decoder[Request] = deriveDecoder[Request]

  implicit val validateWarningEncoder: Encoder[ValidateWarning] = deriveEncoder[ValidateWarning]
  implicit val validateWarningDecoder: Decoder[ValidateWarning] = deriveDecoder[ValidateWarning]
  implicit val validateWarningsEncoder: Encoder[ValidateWarnings] = deriveEncoder[ValidateWarnings]
  implicit val validateWarningsDecoder: Decoder[ValidateWarnings] = deriveDecoder[ValidateWarnings]
  implicit val deployResultEncoder: Encoder[DeployResult] = deriveEncoder[DeployResult]
  implicit val deployResultDecoder: Decoder[DeployResult] = deriveDecoder[DeployResult]


}

object JsonEnDecoders {

  def toResult[T: Decoder](host: Host, response: Response): ZIO[Any, HttpDeployClientException, T] = {
    response match {
      case Response.WithContent(_, body) =>
        decode[T](body) match {
          case Right(values) =>
            ZIO.succeed(values)
          case Left(error) =>
            ZIO.fail(HttpDeployClientException(s"Response could not be decoded for $host: $error", Some(error)))
        }
      case other =>
        ZIO.fail(HttpDeployClientException(s"Unexpected Response for $host: $other"))
    }
  }
}