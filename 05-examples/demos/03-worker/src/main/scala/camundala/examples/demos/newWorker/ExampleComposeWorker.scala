package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.examples.demos.newWorker.ExampleCompose.*
import camundala.worker.{CamundalaWorkerError, EngineRunContext}
import camundala.worker.c7zio.C8Worker
import zio.*

class ExampleComposeWorker(jobWorker: ExampleJobWorker, job2Worker: ExampleJob2Worker)
    extends CompanyCustomWorkerDsl[In, Out]:
  lazy val customTask                                                                             = example
  override def runWorkZIO(in: In): EngineRunContext ?=> IO[CamundalaWorkerError.CustomError, Out] =
    logger.info(s"Running ExampleJob2Worker with $in")
    ZIO
      .foreachPar(Seq[() => IO[CamundalaWorkerError.CustomError, ? <: Product]](
        runExample1,
        runExample2
      )): runWorker =>
        runWorker()
      .flatMap: result =>
        ZIO.logInfo(s"Result: $result")
          .as(Out())
  end runWorkZIO

  private def runExample1()
      : EngineRunContext ?=> IO[CamundalaWorkerError.CustomError, ExampleJob.Out] =
    jobWorker.runWorkFromWorkerUnsafe(ExampleJob.In())
      .mapError: error =>
        CamundalaWorkerError.CustomError(
          s"Error while fetching ExampleJob1:\n- ${error.errorMsg}."
        )
  private def runExample2()
      : EngineRunContext ?=> IO[CamundalaWorkerError.CustomError, ExampleJob2.Out] =
    job2Worker.runWorkFromWorkerUnsafe(ExampleJob2.In())
      .mapError: error =>
        CamundalaWorkerError.CustomError(
          s"Error while fetching ExampleJob2:\n- ${error.errorMsg}."
        )

end ExampleComposeWorker

object ExampleCompose extends CompanyBpmnCustomTaskDsl:

  val topicName     = "publish-tweet2"
  val descr: String = "Creates and adjusts variables for the module creditcard."

  case class In(
      clientKey: Long = 123L,
      approved: Boolean = true,
      myMessage: Option[String] = Some("hello"),
      myTypes: List[MyType] = List(MyType("no", 12), MyType(), MyType())
  )

  object In:
    given ApiSchema[In]  = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec

  case class MyType(doit: String = "yes", why: Int = 42)
  object MyType:
    given ApiSchema[MyType]  = deriveApiSchema
    given InOutCodec[MyType] = deriveInOutCodec

  case class Out(
      myId: Long = 123L,
      myMessage: String = "hello"
  )
  object Out:
    given ApiSchema[Out]  = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec

  lazy val example = customTask(
    In(),
    Out()
  )
end ExampleCompose
