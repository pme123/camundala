package camundala.domain

import io.github.iltotore.iron.*
import io.github.iltotore.iron.jsoniter.given
import sttp.tapir.codec.iron.given

@description(
  "Mocks a REST Service Response (must be handled by the BPF package)."
)
case class MockedServiceResponse[
    ServiceOut // output of service
](
    respStatus: Int,
    respBody: Either[Option[String :| InOutJson], ServiceOut],
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
end MockedServiceResponse

object MockedServiceResponse:

  def success[
      ServiceOut
  ](
      status: Int,
      body: ServiceOut,
      headers: Map[String, String] = Map.empty
  ): MockedServiceResponse[ServiceOut] =
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
  ](status: Int, body: String :| InOutJson): MockedServiceResponse[ServiceOut] =
    MockedServiceResponse(status, Left(Some(body)))

  def error[
      ServiceOut
  ](status: Int): MockedServiceResponse[ServiceOut] =
    MockedServiceResponse(status, Left(None))

  given [ServiceOut: ApiSchema]:ApiSchema[MockedServiceResponse[ServiceOut]] =
    deriveApiSchema[MockedServiceResponse[ServiceOut]]
  given [ServiceOut: InOutCodec]: InOutCodec[MockedServiceResponse[ServiceOut]] =
    deriveInOutCodec[MockedServiceResponse[ServiceOut]]

end MockedServiceResponse

extension (headers: Map[String, String])
  def toHeaders: Seq[Seq[String]] = headers.map {
    case k -> v => Seq(k, v)
  }.toSeq
end extension
