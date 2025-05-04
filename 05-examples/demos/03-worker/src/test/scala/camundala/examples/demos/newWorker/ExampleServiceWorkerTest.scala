package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError
import camundala.worker.CamundalaWorkerError.*
import camundala.examples.demos.newWorker.ExampleService.{Out, *}

class ExampleServiceWorkerTest extends munit.FunSuite:

  lazy val worker = new ExampleServiceWorker()

  test("apiUri should return expected URI") {
    assertEquals(
      worker.apiUri(In()).toString(),
      "NOT-SET/YourPath"
    )
  }

  test("validate should accept valid input") {
    val input = In(
      clientKey = 123L,
      approved = true,
      myMessage = Some("test message"),
      myTypes = List(MyType("test", 1))
    )

    assertEquals(
      worker.validate(input),
      Right(input)
    )
  }

  test("serviceTask should match example task") {
    assertEquals(
      worker.serviceTask,
      example
    )
  }

  test("should handle default input values") {
    val input = In()

    assertEquals(
      input,
      In(
        clientKey = 123L,
        approved = true,
        myMessage = Some("hello"),
        myTypes = List(MyType("no", 12), MyType(), MyType())
      )
    )
  }

  test("should handle custom input values") {
    val customInput = In(
      clientKey = 999L,
      approved = false,
      myMessage = Some("custom message"),
      myTypes = List(MyType("custom", 100))
    )

    assertEquals(
      customInput.clientKey,
      999L
    )
    assertEquals(
      customInput.approved,
      false
    )
    assertEquals(
      customInput.myMessage,
      Some("custom message")
    )
    assertEquals(
      customInput.myTypes,
      List(MyType("custom", 100))
    )
  }

  test("output case class should have correct default values") {
    val defaultOutput = Out()

    assertEquals(
      defaultOutput,
      Out(
        myId = 123L,
        myMessage = "hello"
      )
    )
  }

  test("MyType should have correct default values") {
    val defaultMyType = MyType()

    assertEquals(
      defaultMyType,
      MyType(
        doit = "yes",
        why = 42
      )
    )
  }

end ExampleServiceWorkerTest
