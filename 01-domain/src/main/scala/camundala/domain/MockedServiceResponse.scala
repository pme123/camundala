package camundala.domain

@description(
  "Mocks a REST Service Response (must be handled by the BPF package)."
)
case class MockedServiceResponse[
    ServiceOut // output of service
](
    respStatus: Int,
    respBody: Either[Option[Json], ServiceOut],
    respHeaders: Seq[Seq[String]] = Seq.empty
):
  def withHeader(
      key: String,
      value: String
  ): MockedServiceResponse[ServiceOut] =
    copy(respHeaders = respHeaders :+ Seq(key, value))

  def withHeaders(
      headers: Map[String, String]
  ): MockedServiceResponse[ServiceOut] =
    copy(respHeaders = respHeaders ++ headers.toSeq.map { case k -> v =>
      Seq(k, v)
    })

  // be sure ServiceOut is set!
  def unsafeBody: ServiceOut = respBody.toOption.get
  def headersAsMap: Map[String, String] =
    respHeaders
      .map(_.toList)
      .collect { case key :: value :: _ => key -> value }
      .toMap

object MockedServiceResponse:

  def success[
      ServiceOut
  ](status: Int, body: ServiceOut, headers: Map[String, String] = Map.empty): MockedServiceResponse[ServiceOut] =
    MockedServiceResponse(status, Right(body), headers.toHeaders)

  def success200[
      ServiceOut
  ](body: ServiceOut, headers: Map[String, String] = Map.empty): MockedServiceResponse[ServiceOut] =
    success(200, body, headers)

  def success201[
      ServiceOut
  ](body: ServiceOut, headers: Map[String, String] = Map.empty): MockedServiceResponse[ServiceOut] =
    success(201, body, headers)

  lazy val success204: MockedServiceResponse[NoOutput] =
    success(204, NoOutput())

  def error[
      ServiceOut
  ](status: Int, body: Json): MockedServiceResponse[ServiceOut] =
    MockedServiceResponse(status, Left(Some(body)))

  def error[
      ServiceOut
  ](status: Int): MockedServiceResponse[ServiceOut] =
    MockedServiceResponse(status, Left(None))

  implicit def tapirSchema[ServiceOut: Schema]: Schema[MockedServiceResponse[ServiceOut]] =
    Schema.derived[MockedServiceResponse[ServiceOut]]

  implicit def mockedHttpResponseEncoder[ServiceOut: Encoder]
      : Encoder[MockedServiceResponse[ServiceOut]] =
    Encoder.instance { response =>
      Json.obj(
        "respStatus" -> Json.fromInt(response.respStatus),
        "respBody" -> (
          response.respBody match
            case Right(value) => value.asJson
            case Left(err) => err.getOrElse(Json.Null)
        ),
        "respHeaders" -> response.respHeaders
          .map(_.map(Json.fromString))
          .asJson
      )
    }
  implicit def mockedHttpResponseDecoder[ServiceOut: Decoder]
      : Decoder[MockedServiceResponse[ServiceOut]] =
    Decoder.instance { cursor =>
      for {
        respStatus <- cursor.downField("respStatus").as[Int]
        respBody <-
          if (respStatus < 300)
            cursor.downField("respBody").as[ServiceOut].map(Right(_))
          else cursor.downField("respBody").as[Option[Json]].map(Left(_))
        respHeaders <- cursor.downField("respHeaders").as[Seq[Seq[String]]]
      } yield MockedServiceResponse(respStatus, respBody, respHeaders)
    }
end MockedServiceResponse

extension(headers: Map[String, String])
  def toHeaders: Seq[Seq[String]] = headers.map {
    case k ->v=> Seq(k,v)
  }.toSeq
end extension

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
