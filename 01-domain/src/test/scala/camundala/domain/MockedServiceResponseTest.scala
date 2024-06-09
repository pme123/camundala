package camundala.domain

class MockedServiceResponseTest extends munit.FunSuite:

  lazy val myMockResp = TestWithMock()

  test("testCodecSuccess") {
    val json = myMockResp.asJson
    assertEquals(myMockResp, json.as[TestWithMock].getOrElse(fail("Decoding Problem")))
  }

  lazy val myMockErrorResp = TestWithMock(
    MockedServiceResponse.error(444, Json.fromString("ERROR")),
    MockedServiceResponse.error(444)
  )

  test("testCodecError") {
    val json = myMockErrorResp.asJson
    assertEquals(myMockErrorResp, json.as[TestWithMock].getOrElse(fail("Decoding Problem")))
  }

end MockedServiceResponseTest

lazy val seqSuccess = Seq(Success())
type ServiceOut2 = Seq[Success]

case class TestWithMock(
    myMock: MockedServiceResponse[Success] = MockedServiceResponse.success(122, Success()),
    noOutputMock: MockedServiceResponse[NoOutput] = MockedServiceResponse.success(122, NoOutput())
)

object TestWithMock:
  given ApiSchema[TestWithMock] = deriveApiSchema
  given InOutCodec[TestWithMock] = deriveCodec
end TestWithMock

case class Error(msg: String = "failed")
object Error:
  given ApiSchema[Error] = deriveApiSchema
  given InOutCodec[Error] = deriveCodec
end Error

case class Success(ok: String = "hello")

object Success:
  given ApiSchema[Success] = deriveApiSchema
  given InOutCodec[Success] = deriveCodec
end Success
