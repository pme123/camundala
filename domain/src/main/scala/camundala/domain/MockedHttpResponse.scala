package camundala.domain

import io.circe.{DecodingFailure, HCursor}
import io.circe.Decoder.Result
import io.circe.syntax.*

import java.time.LocalDate

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

  implicit def circeCodec[OutS: CirceCodec, OutE: CirceCodec]
      : CirceCodec[MockedHttpResponse[OutS, OutE]] =
    deriveCodec[MockedHttpResponse[OutS, OutE]]

end MockedHttpResponse

implicit def seqCodec[T: CirceCodec]: CirceCodec[Seq[T]] =
  new CirceCodec[Seq[T]] {
    final def apply(c: HCursor): Result[Seq[T]] =
      println(s"HCURSOR: ${c.values}")
      val jsons = c.values.get.toSeq.map(_.as[T])
      val (lefts, rights) = jsons.partition(_.isLeft)
      if (lefts.nonEmpty) Left(DecodingFailure("Problem decoding Seq", lefts.collect { case Left(t) => t.history }.flatten.toList))
      else Right(rights.collect { case Right(a) => a })
    final def apply(a: Seq[T]): Json = Json.arr(a.map(_.asJson): _*)
  }/*
implicit def stringCodec: CirceCodec[String] =
  new CirceCodec[String] {

    final def apply(c: HCursor): Result[String] = c.as[String]

    final def apply(a: String): Json = Json.fromString(a)
  }
*/
//implicit def mySeqCodec[T: CirceCodec]: CirceCodec[Seq[T]] = deriveCodec[Seq[T]]

