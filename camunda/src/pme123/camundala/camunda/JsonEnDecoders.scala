package pme123.camundala.camunda

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}
import pme123.camundala.camunda.httpDeployClient.HttpDeployClientException
import pme123.camundala.camunda.service.restService.Request.{Auth, Host}
import pme123.camundala.camunda.service.restService.RequestBody.Part
import pme123.camundala.camunda.service.restService._
import pme123.camundala.camunda.xml.{ValidateWarning, ValidateWarnings}
import pme123.camundala.model.bpmn.{Sensitive, VariableDef, VariableDefs, VariableType}
import zio.ZIO

trait JsonEnDecoders {
  // model
  implicit val SensitiveEncoder: Encoder[Sensitive] = deriveEncoder[Sensitive]
  implicit val SensitiveDecoder: Decoder[Sensitive] = deriveDecoder[Sensitive]

  // camunda
  implicit val AuthEncoder: Encoder[Auth] = deriveEncoder[Auth]
  implicit val AuthDecoder: Decoder[Auth] = deriveDecoder[Auth]
  implicit val HostEncoder: Encoder[Host] = deriveEncoder[Host]
  implicit val HostDecoder: Decoder[Host] = deriveDecoder[Host]

  implicit val MethodEncoder: Encoder[RequestMethod] = deriveEncoder[RequestMethod]
  implicit val MethodDecoder: Decoder[RequestMethod] = deriveDecoder[RequestMethod]
  implicit val RequestPathEncoder: Encoder[RequestPath] = deriveEncoder[RequestPath]
  implicit val RequestPathDecoder: Decoder[RequestPath] = deriveDecoder[RequestPath]
  implicit val QueryParamsEncoder: Encoder[QueryParams] = deriveEncoder[QueryParams]
  implicit val QueryParamsDecoder: Decoder[QueryParams] = deriveDecoder[QueryParams]
  implicit val RequestHeadersEncoder: Encoder[RequestHeaders] = deriveEncoder[RequestHeaders]
  implicit val RequestHeadersDecoder: Decoder[RequestHeaders] = deriveDecoder[RequestHeaders]
  implicit val PartEncoder: Encoder[Part] = deriveEncoder[Part]
  implicit val PartDecoder: Decoder[Part] = deriveDecoder[Part]
  implicit val RequestBodyEncoder: Encoder[RequestBody] = deriveEncoder[RequestBody]
  implicit val RequestBodyDecoder: Decoder[RequestBody] = deriveDecoder[RequestBody]
  implicit val ResponseReadEncoder: Encoder[ResponseRead] = deriveEncoder[ResponseRead]
  implicit val ResponseReadDecoder: Decoder[ResponseRead] = deriveDecoder[ResponseRead]
  implicit val MockDataEncoder: Encoder[MockData] = deriveEncoder[MockData]
  implicit val MockDataDecoder: Decoder[MockData] = deriveDecoder[MockData]
  implicit val VariableTypeEncoder: Encoder[VariableType] = deriveEncoder[VariableType]
  implicit val VariableTypeDecoder: Decoder[VariableType] = deriveDecoder[VariableType]
  implicit val VariableDefEncoder: Encoder[VariableDef] = deriveEncoder[VariableDef]
  implicit val VariableDefDecoder: Decoder[VariableDef] = deriveDecoder[VariableDef]
  implicit val VariableDefsEncoder: Encoder[VariableDefs] = deriveEncoder[VariableDefs]
  implicit val VariableDefsDecoder: Decoder[VariableDefs] = deriveDecoder[VariableDefs]
  implicit val RequestEncoder: Encoder[Request] = deriveEncoder[Request]
  implicit val RequestDecoder: Decoder[Request] = deriveDecoder[Request]

  implicit val ValidateWarningEncoder: Encoder[ValidateWarning] = deriveEncoder[ValidateWarning]
  implicit val ValidateWarningDecoder: Decoder[ValidateWarning] = deriveDecoder[ValidateWarning]
  implicit val ValidateWarningsEncoder: Encoder[ValidateWarnings] = deriveEncoder[ValidateWarnings]
  implicit val ValidateWarningsDecoder: Decoder[ValidateWarnings] = deriveDecoder[ValidateWarnings]
  implicit val DeployResultEncoder: Encoder[DeployResult] = deriveEncoder[DeployResult]
  implicit val DeployResultDecoder: Decoder[DeployResult] = deriveDecoder[DeployResult]


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
