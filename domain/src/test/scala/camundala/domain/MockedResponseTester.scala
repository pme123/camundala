package camundala.domain

import io.circe.syntax.*

object MockedResponseTester extends App:
  case class Error(msg: String = "failedxx")

  object Error:
    given Schema[Error] = Schema.derived

    given CirceCodec[Error] = deriveCodec
  end Error

  case class Success(ok: String = "hello")

  object Success:
    given Schema[Success] = Schema.derived

    given CirceCodec[Success] = deriveCodec
  end Success
  
  type OutE = Seq[Success]
  val mySeq = Seq(Success())
  val mocked: MockedHttpResponse[OutE, Error] = MockedHttpResponse.success200(mySeq)
  private val json: Json = mocked.asJson
  println(
    // json.as[Seq[Error]]
    json.as[MockedHttpResponse[OutE, Error]]
  )
end MockedResponseTester


