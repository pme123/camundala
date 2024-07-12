package camundala.camunda7.worker

import camundala.domain.NoOutput
import java.time.LocalDate
import camundala.worker.CamundalaWorkerError.*
import camundala.domain.*

class RestApiClientTest extends munit.FunSuite, RestApiClient:

  type ServiceOut = NoOutput

  test("RestApiClientTest NoOutput"):
    assertEquals(
      Right(NoOutput()),
      decodeResponse[ServiceOut]("")
    )

  test("RestApiClientTest no NoOutput"):
    assertEquals(
      Left(ServiceBadBodyError("There is no body in the response and the ServiceOut is not NoOutput.")),
      decodeResponse[String]("")
    )

  test("RestApiClientTest  with Output"):
    assertEquals(
      Right(MyClass()),
      decodeResponse[MyClass]("{ \"value\": 12}")
    )

  case class MyClass(value:Int = 12)  
  object MyClass:
    given InOutCodec[MyClass] = deriveInOutCodec
    
end RestApiClientTest
