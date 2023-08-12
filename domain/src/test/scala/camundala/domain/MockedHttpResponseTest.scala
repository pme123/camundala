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
type OutE2 = Seq[Error]

case class TestWithMock(
  myMock: MockedHttpResponse[Success, Error] = MockedHttpResponse.success(122, Success()),
  noOutputMock: MockedHttpResponse[NoOutput, NoOutput] = MockedHttpResponse.success(122, NoOutput()),
  @description(outputServiceMockDescr(seqSuccess))
  seqOutputMock: MockedHttpResponse[OutS2, OutE2] = MockedHttpResponse.success200[OutS2, OutE2](seqSuccess),
)


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