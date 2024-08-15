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
  test("RestApiClientTest NoOutput OK"):
    assertEquals(
      Right(NoOutput()),
      decodeResponse[ServiceOut]("OK")
    )

  test("RestApiClientTest Bad body OK"):
    assertEquals(
      Left(
        ServiceBadBodyError(
          errorMsg =
            """Problem creating body from response.
              |NonEmptyList(ParsingFailure: expected json value got 'OK' (line 1, column 1))
              |BODY: OK""".stripMargin
        )
      ),
      decodeResponse[MyClass]("OK")
    )

  test("RestApiClientTest no NoOutput"):
    assertEquals(
      Left(ServiceBadBodyError(
        "There is no body in the response and the ServiceOut is neither NoOutput nor Option (Class is class java.lang.String)."
      )),
      decodeResponse[String]("")
    )

  test("RestApiClientTest  with Output"):
    assertEquals(
      Right(MyClass()),
      decodeResponse[MyClass]("{ \"value\": 12}")
    )

  test("RestApiClientTest  with optional Output"):
    assertEquals(
      Right(None),
      decodeResponse[Option[MyClass]]("")
    )

  test("hasNoOutput"):
    assertEquals(
      true,
      hasNoOutput[NoOutput]()
    )
  test("hasNoOutput false"):
    assertEquals(
      false,
      hasNoOutput[String]()
    )

  case class MyClass(value: Int = 12)
  object MyClass:
    given InOutCodec[MyClass] = deriveInOutCodec

end RestApiClientTest
