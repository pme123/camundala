package camundala.examples.demos.newWorker

import camundala.bpmn.CustomTask
import camundala.domain.*
import camundala.examples.demos.newWorker.ExampleJob.*
import camundala.worker.CamundalaWorkerError
import camundala.worker.c8zio.C8Worker

object ExampleJobHandler extends CompanyCustomWorkerDsl[In, Out]:
  lazy val customTask = example
  def runWork(in: In): Either[CamundalaWorkerError.CustomError, Out] = ???
end ExampleJobHandler

object ExampleJob extends CompanyBpmnCustomTaskDsl:

  val topicName     = "publish-tweet"
  val descr: String = "Creates and adjusts variables for the module creditcard."

  case class In(
      myId: Long = 123L,
      myMessage: String = "hello"
  )

  object In:
    given ApiSchema[In]  = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec

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
