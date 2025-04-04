package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError
import camundala.worker.CamundalaWorkerError.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.*
import camundala.examples.demos.newWorker.ExampleService.{Out, *}

object ExampleServiceWorkerSpec extends ZIOSpecDefault:

  def spec = suite("ExampleServiceWorkerSpec")(
    test("validate should accept valid input") {
      // Arrange
      val worker = new ExampleServiceWorker()
      val input = ExampleService.In(
        clientKey = 123L,
        approved = true,
        myMessage = Some("test message"),
        myTypes = List(ExampleService.MyType("test", 1))
      )

      // Act
      val result = worker.runWorkFromWorkerUnsafe(input)

      // Assert
      assertZIO(result.flip)(equalTo(ValidatorError("Not valid input.")))
    }
  ) @@ sequential

end ExampleServiceWorkerSpec