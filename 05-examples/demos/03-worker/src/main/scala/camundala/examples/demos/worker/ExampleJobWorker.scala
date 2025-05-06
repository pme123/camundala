package camundala.examples.demos.worker

import camundala.domain.*
import camundala.examples.demos.newWorker.CompanyBpmnCustomTaskDsl
import camundala.examples.demos.worker.ExampleJobWorker.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.CustomError
import zio.*
@SpringConfiguration
class ExampleJobWorker extends CompanyCustomWorkerDsl[In, Out]:

  lazy val customTask = example

  override def runWorkZIO(in: In): EngineRunContext ?=> IO[CustomError, Out] =
    logger.info(s"Running ExampleJobWorker OLD with $in")
    ZIO.sleep(500.millis).as(Out())
    // Left(CamundalaWorkerError.CustomError("Not implemented yet."))
end ExampleJobWorker

object ExampleJobWorker extends CompanyBpmnCustomTaskDsl:

  val topicName     = "publish-tweet"
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
      myMessage: String = "hello",
      count: Int = 2
  )
  object Out:
    given ApiSchema[Out]  = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec

  lazy val example = customTask(
    In(),
    Out()
  )
end ExampleJobWorker
