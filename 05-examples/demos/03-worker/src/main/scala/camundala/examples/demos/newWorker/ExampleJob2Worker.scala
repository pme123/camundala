package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.examples.demos.newWorker.ExampleJob2.*
import camundala.worker.CamundalaWorkerError
import camundala.worker.c7zio.C8Worker
import zio.*

object ExampleJob2Worker extends CompanyCustomWorkerDsl[In, Out]:
  lazy val customTask = example
  override def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] =
    logger.info(s"Running ExampleJob2Worker with $in")
    Thread.sleep(2000)
    Right(Out())
end ExampleJob2Worker

object ExampleJob2 extends CompanyBpmnCustomTaskDsl:

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
end ExampleJob2

