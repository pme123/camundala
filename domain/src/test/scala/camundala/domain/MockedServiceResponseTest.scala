package camundala.domain

import org.junit.*
import org.junit.Assert.*

import io.circe.syntax.*
import io.circe.*

class MockedServiceResponseTest:
 
  lazy val myMockResp =  TestWithMock()

  @Test
  def testCodecSuccess(): Unit =
    val json = myMockResp.asJson
    assertEquals(myMockResp, json.as[TestWithMock].getOrElse(fail()))

  lazy val myMockErrorResp =  TestWithMock(
    MockedServiceResponse.error(444, Json.fromString("ERROR")),
    MockedServiceResponse.error(444),
  )

  @Test
  def testCodecError(): Unit =
    val json = myMockErrorResp.asJson
    assertEquals(myMockErrorResp, json.as[TestWithMock].getOrElse(fail()))

end MockedServiceResponseTest

lazy val seqSuccess = Seq(Success())
type ServiceOut2 = Seq[Success]

case class TestWithMock(
                         myMock: MockedServiceResponse[Success] = MockedServiceResponse.success(122, Success()),
                         @description(outputServiceMockDescr(seqSuccess))
                         noOutputMock: MockedServiceResponse[NoOutput] = MockedServiceResponse.success(122, NoOutput()),
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