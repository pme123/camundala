package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.examples.demos.newWorker.ExampleJob.*
import camundala.worker.CamundalaWorkerError
import camundala.worker.c7zio.C8Worker

object ExampleJobWorker extends CompanyCustomWorkerDsl[In, Out]:
  lazy val customTask = example
  def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
    logger.info(s"Running ExampleJobWorker with $in")
    Thread.sleep(3000)
    Right(Out())
end ExampleJobWorker

object ExampleJob extends CompanyBpmnCustomTaskDsl:

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
      myMessage: String = "hello"
  )
  object Out:
    given ApiSchema[Out]  = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec

  lazy val example = customTask(
    In(),
    Out()
  )
end ExampleJob
