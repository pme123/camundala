package pme123.camundala.camunda

import io.circe.{Decoder, Encoder}
import io.circe.refined._
import io.circe.generic.semiauto.{deriveEncoder, deriveDecoder}
import pme123.camundala.camunda.service.restService.Request.{Auth, Host}
import pme123.camundala.camunda.service.restService.{QueryParams, Request, RequestBody, RequestHeaders, RequestMethod, RequestPath, ResponseRead}
import pme123.camundala.model.deploy.Sensitive

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
  implicit val requestBodyEncoder: Encoder[RequestBody] = deriveEncoder[RequestBody]
  implicit val requestBodyDecoder: Decoder[RequestBody] = deriveDecoder[RequestBody]
  implicit val responseReadEncoder: Encoder[ResponseRead] = deriveEncoder[ResponseRead]
  implicit val responseReadDecoder: Decoder[ResponseRead] = deriveDecoder[ResponseRead]
  implicit val requestEncoder: Encoder[Request] = deriveEncoder[Request]
  implicit val requestDecoder: Decoder[Request] = deriveDecoder[Request]
}
