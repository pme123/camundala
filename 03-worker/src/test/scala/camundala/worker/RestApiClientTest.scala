package camundala.worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*

class RestApiClientTest extends munit.FunSuite, RestApiClient:

  type ServiceOut = NoOutput

  test("RestApiClientTest NoOutput"):
    assertEquals(
      decodeResponse[ServiceOut](""),
      Right(NoOutput())
    )
  test("RestApiClientTest NoOutput OK"):
    assertEquals(
      decodeResponse[ServiceOut]("OK"),
      Right(NoOutput())
    )

  test("RestApiClientTest Seq OK"):
    assertEquals(
      decodeResponse[Seq[String]]("[\"hello\"]"),
      Right(Seq("hello"))
    )

  test("RestApiClientTest Bad body OK"):
    assertEquals(
      decodeResponse[MyClass]("OK"),
      Left(
        ServiceBadBodyError(
          errorMsg =
            """Problem creating body from response.
              |NonEmptyList(ParsingFailure: expected json value got 'OK' (line 1, column 1))
              |BODY: OK""".stripMargin
        )
      )
    )

  test("RestApiClientTest no NoOutput"):
    assertEquals(
      decodeResponse[String](""),
      Left(ServiceBadBodyError(
        "There is no body in the response and the ServiceOut is neither NoOutput nor Option (Class is class java.lang.String)."
      ))
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
