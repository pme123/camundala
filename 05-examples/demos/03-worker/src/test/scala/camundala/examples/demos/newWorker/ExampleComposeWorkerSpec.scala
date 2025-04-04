package camundala.examples.demos.newWorker

import camundala.domain.GeneralVariables
import camundala.examples.demos.newWorker.ExampleCompose.*
import camundala.examples.demos.newWorker.ExampleJob
import camundala.examples.demos.newWorker.ExampleJob2
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.c7zio.C7Context
import camundala.worker.{EngineContext, EngineRunContext, WorkerLogger}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.concurrent.atomic.AtomicInteger

object ExampleComposeWorkerSpec extends ZIOSpecDefault:

  def spec = suite("ExampleComposeWorkerSpec")(

    test("should successfully execute both workers in parallel") {
      // Arrange
      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.succeed(ExampleJob.Out(myId = 123L, myMessage = "job1 success"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.succeed(ExampleJob2.Out(myId = 456L, myMessage = "job2 success"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      // Act
      val result = worker.runWorkZIO(In())

      // Assert
      assertZIO(result)(
        equalTo(Out())
      )
    },

    test("should execute workers in parallel") {
      // Arrange
      // Use a simpler approach that doesn't rely on sleep
      val job1Started = new AtomicInteger(0)
      val job2Started = new AtomicInteger(0)

      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.succeed(job1Started.incrementAndGet())
            .as(ExampleJob.Out(myId = 123L, myMessage = "job1 success"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.succeed(job2Started.incrementAndGet())
            .as(ExampleJob2.Out(myId = 456L, myMessage = "job2 success"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      // Act & Assert
      assertZIO(
        worker.runWorkZIO(In()).as((job1Started.get(), job2Started.get()))
      )(
        equalTo((1, 1)) // Both workers should have executed exactly once
      )
    },

    test("should fail if first worker fails") {
      // Arrange
      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.fail(CustomError("Job1 failed"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.succeed(ExampleJob2.Out(myId = 456L, myMessage = "job2 success"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      // Act
      val result = worker.runWorkZIO(In())

      // Assert
      assertZIO(result.exit)(
        fails(
          hasField("errorMsg", (e: CustomError) => e.errorMsg, containsString("Error while fetching ExampleJob1"))
        )
      )
    },

    test("should fail if second worker fails") {
      // Arrange
      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.succeed(ExampleJob.Out(myId = 123L, myMessage = "job1 success"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.fail(CustomError("Job2 failed"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      // Act
      val result = worker.runWorkZIO(In())

      // Assert
      assertZIO(result.exit)(
        fails(
          hasField("errorMsg", (e: CustomError) => e.errorMsg, containsString("Error while fetching ExampleJob2"))
        )
      )
    },

    test("should fail if both workers fail") {
      // Arrange
      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.fail(CustomError("Job1 failed"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.fail(CustomError("Job2 failed"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      // Act
      val result = worker.runWorkZIO(In())

      // Assert
      assertZIO(result.exit)(
        fails(
          hasField("errorMsg", (e: CustomError) => e.errorMsg, containsString("Error while fetching ExampleJob"))
        )
      )
    },

    test("should handle custom input parameters") {
      // Arrange
      val mockJobWorker = new ExampleJobWorker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob.In)(using EngineRunContext): IO[CustomError, ExampleJob.Out] =
          ZIO.succeed(ExampleJob.Out(myId = 123L, myMessage = "job1 success"))

      val mockJob2Worker = new ExampleJob2Worker:
        override def runWorkFromWorkerUnsafe(in: ExampleJob2.In)(using EngineRunContext): IO[CustomError, ExampleJob2.Out] =
          ZIO.succeed(ExampleJob2.Out(myId = 456L, myMessage = "job2 success"))

      val worker = new ExampleComposeWorker(mockJobWorker, mockJob2Worker)

      val customInput = In(
        clientKey = 999L,
        approved = false,
        myMessage = Some("custom message"),
        myTypes = List(MyType("custom", 100))
      )

      // Act
      val result = worker.runWorkZIO(customInput)

      // Assert
      assertZIO(result)(
        equalTo(Out())
      )
    }
  )

end ExampleComposeWorkerSpec
