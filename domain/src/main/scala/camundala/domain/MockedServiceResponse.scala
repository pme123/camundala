package camundala.domain

import io.circe.syntax.*

@description(
  "Mocks a REST Service Response (must be handled by the BPF package)."
)
case class MockedServiceResponse[
    OutS // output of service
](
    respStatus: Int,
    respBody: Either[String, OutS],
    respHeaders: Seq[Seq[String]] = Seq.empty
):
  def withHeader(
      key: String,
      value: String
  ): MockedServiceResponse[OutS] =
    copy(respHeaders = respHeaders :+ Seq(key, value))

object MockedServiceResponse:

  def success[
      OutS
  ](status: Int, body: OutS):MockedServiceResponse[OutS] =
    MockedServiceResponse(status, Right(body))

  def success200[
      OutS
  ](body: OutS): MockedServiceResponse[OutS] =
    success(200, body)

  def success201[
      OutS
  ](body: OutS): MockedServiceResponse[OutS] =
    success(201, body)

  lazy val success204: MockedServiceResponse[NoOutput] =
    success(204, NoOutput())

  def error[
      OutS
  ](status: Int, body: String = "No Output"): MockedServiceResponse[OutS] =
    MockedServiceResponse(status, Left(body))

  implicit def tapirSchema[OutS: Schema]
      : Schema[MockedServiceResponse[OutS]] =
    Schema.derived[MockedServiceResponse[OutS]]

  implicit def mockedHttpResponseEncoder[OutS: Encoder]
      : Encoder[MockedServiceResponse[OutS]] =
    Encoder.instance { response =>
      Json.obj(
        "respStatus" -> Json.fromInt(response.respStatus),
        "respBody" -> (
          response.respBody match
            case Right(value) => value.asJson
            case Left(err) => Json.fromString(err.toString)
        ),
        "respHeaders" -> response.respHeaders
          .map(_.map(Json.fromString))
          .asJson
      )
    }
  implicit def mockedHttpResponseDecoder[OutS: Decoder]
      : Decoder[MockedServiceResponse[OutS]] =
    Decoder.instance { cursor =>
      for {
        respStatus <- cursor.downField("respStatus").as[Int]
        respBody <-
          if (respStatus < 300)
            cursor.downField("respBody").as[OutS].map(Right(_))
          else cursor.downField("respBody").as[String].map(Left(_))
        respHeaders <- cursor.downField("respHeaders").as[Seq[Seq[String]]]
      } yield MockedServiceResponse(respStatus, respBody, respHeaders)
    }
end MockedServiceResponse

