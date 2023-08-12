package camundala.domain

import io.circe.syntax.*

@description(
  "Mocks a REST Service Response (must be handled by the BPF package)."
)
case class MockedHttpResponse[
    OutS, // output of service
    OutE // output of  service in case of error
](
    respStatus: Int,
    respBody: Either[OutE, OutS],
    respHeaders: Seq[Seq[String]] = Seq.empty
):
  def withHeader(key: String, value: String): MockedHttpResponse[OutS, OutE] =
    copy(respHeaders = respHeaders :+ Seq(key, value))

object MockedHttpResponse:

  def success[
      OutS,
      OutE
  ](status: Int, body: OutS): MockedHttpResponse[OutS, OutE] =
    MockedHttpResponse(status, Right(body))

  def success200[
      OutS,
      OutE
  ](body: OutS): MockedHttpResponse[OutS, OutE] =
    success(200, body)

  def success201[
      OutS,
      OutE
  ](body: OutS): MockedHttpResponse[OutS, OutE] =
    success(201, body)

  lazy val success204: MockedHttpResponse[NoOutput, NoOutput] =
    success(204, NoOutput())

  def error[
      OutS,
      OutE
  ](status: Int, body: OutE = NoOutput()): MockedHttpResponse[OutS, OutE] =
    MockedHttpResponse(status, Left(body))

  implicit def tapirSchema[OutS: Schema, OutE: Schema]
      : Schema[MockedHttpResponse[OutS, OutE]] =
    Schema.derived[MockedHttpResponse[OutS, OutE]]

  /*
  implicit def circeCodec[OutS: CirceCodec, OutE: CirceCodec]
      : CirceCodec[MockedHttpResponse[OutS, OutE]] =
    new CirceCodec[MockedHttpResponse[OutS, OutE]] {

      final def apply(c: HCursor): Result[MockedHttpResponse[OutS, OutE]] =
        for {
          respStatus <- c.downField("respStatus").as[Int]
          respBody <- if(respStatus < 300) c.downField("respBody").as[OutS].map(Right(_))
          else c.downField("respBody").as[OutE].map(Left(_))
          respHeaders <- c.downField("respHeaders").as[Seq[Seq[String]]]
        } yield MockedHttpResponse(respStatus, respBody, respHeaders)

      final def apply(mResp: MockedHttpResponse[OutS, OutE]): Json = Json.obj(
        ("respStatus", mResp.respStatus.asJson),
        (
          "respBody",
          mResp.respBody match
            case Right(value) => value.asJson
            case Left(err) => err.asJson
        ),
        ("respHeaders", mResp.respHeaders.asJson)
      )
    }
  end circeCodec
   */
  implicit def mockedHttpResponseEncoder[OutS: Encoder, OutE: Encoder]
      : Encoder[MockedHttpResponse[OutS, OutE]] =
    Encoder.instance { response =>
      Json.obj(
        "respStatus" -> Json.fromInt(response.respStatus),
        "respBody" -> (
          response.respBody match
            case Right(value) => value.asJson
            case Left(err) => err.asJson
        ),
        "respHeaders" -> response.respHeaders
          .map(_.map(Json.fromString))
          .asJson
      )
    }
  implicit def mockedHttpResponseDecoder[OutS: Decoder, OutE: Decoder]
      : Decoder[MockedHttpResponse[OutS, OutE]] =
    Decoder.instance { cursor =>
      for {
        respStatus <- cursor.downField("respStatus").as[Int]
        respBody <-
          if (respStatus < 300)
            cursor.downField("respBody").as[OutS].map(Right(_))
          else cursor.downField("respBody").as[OutE].map(Left(_))
        respHeaders <- cursor.downField("respHeaders").as[Seq[Seq[String]]]
      } yield MockedHttpResponse(respStatus, respBody, respHeaders)
    }
end MockedHttpResponse
/*
// needed for mocked Results of Seq
implicit def seqCodec[T: CirceCodec]: CirceCodec[Seq[T]] =
  new CirceCodec[Seq[T]] {
    final def apply(c: HCursor): Result[Seq[T]] =
      val jsons = c.values.get.toSeq.map(_.as[T])
      val (lefts, rights) = jsons.partition(_.isLeft)
      if (lefts.nonEmpty)
        Left(
          DecodingFailure(
            "Problem decoding Seq",
            lefts.collect { case Left(t) => t.history }.flatten.toList
          )
        )
      else Right(rights.collect { case Right(a) => a })
    final def apply(a: Seq[T]): Json = Json.arr(a.map(_.asJson): _*)
  }
end seqCodec
*/