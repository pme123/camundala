package camundala.domain

import org.junit.*
import org.junit.Assert.*

import io.circe.syntax.*
import io.circe.*

class MockedHttpResponseTest:
 
  lazy val myMockResp =  TestWithMock()

  @Test
  def testCodecSuccess(): Unit =
    val json = myMockResp.asJson
    assertEquals(myMockResp, json.as[TestWithMock].getOrElse(fail()))

  lazy val myMockErrorResp =  TestWithMock(
    MockedHttpResponse.error(444, Error()),
    MockedHttpResponse.error(444),
    MockedHttpResponse.error(444, Seq(Error())),
  )

  @Test
  def testCodecError(): Unit =
    val json = myMockErrorResp.asJson
    assertEquals(myMockErrorResp, json.as[TestWithMock].getOrElse(fail()))

end MockedHttpResponseTest

lazy val seqSuccess = Seq(Success())
type OutS2 = Seq[Success]
// lazy val myMockResp =  MockedHttpResponse(122, Json.fromString("hello")) //
case class TestWithMock(
  myMock: MockedHttpResponse[Success, Error] = MockedHttpResponse.success(122, Success()),
  noOutputMock: MockedHttpResponse[NoOutput, NoOutput] = MockedHttpResponse.success(122, NoOutput()),
  seqOutputMock: MockedHttpResponse[OutS2, Seq[Error]] = MockedHttpResponse.success200(seqSuccess),
) extends MockServiceSupport[NoOutput, Seq[Success], Error]:
  override def outputServiceMock: Option[MockedHttpResponse[OutS2, Error]] = ???

  override def outputMock: Option[NoOutput] = ???

  override def servicesMocked: Boolean = ???

object TestWithMock:
    given Schema[TestWithMock] = Schema.derived
    given CirceCodec[TestWithMock] = deriveCodec
end TestWithMock

case class Error(msg: String = "failed")
object Error:
    given Schema[Error] = Schema.derived
    given CirceCodec[Error] = deriveCodec
end Error 

case class Success(ok: String = "hello")

object Success:
    given Schema[Success] = Schema.derived
    given CirceCodec[Success] = deriveCodec
end Success