package camundala.examples.demos.newWorker

import camundala.domain.*
import camundala.examples.demos.newWorker
import camundala.examples.demos.newWorker.ExampleService.{Out, *}
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.{CamundalaWorkerError, ServiceResponse}
import camundala.worker.c7zio.C8Worker
import sttp.client3.UriContext
import zio.*

class ExampleServiceWorker extends CompanyServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]:
  lazy val customTask = example

  override def validate(in: In): Either[ValidatorError, In] =
    logger.info(s"Running ExampleServiceWorker with $in")
    Left(ValidatorError("Not valid input."))

  override def outputMapper(
      serviceOut: ServiceResponse[ServiceOut],
      in: In
  ): Either[ServiceMappingError, Out] =
    Right(Out())

  def serviceTask: ServiceTask[In, Out, ServiceIn, ServiceOut] = example

  def apiUri(in: In) = uri"NOT-SET/YourPath"
end ExampleServiceWorker

object ExampleService extends CompanyBpmnServiceTaskDsl:

  val topicName     = "service-task-example"
  val descr: String = "Just calls a service."
  type ServiceOut = Out
  type ServiceIn  = In
  lazy val serviceInExample = In()
  lazy val serviceMock      = MockedServiceResponse.success200(Out())

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

  lazy val example: ServiceTask[In, Out, ServiceIn, ServiceOut] = serviceTask(
    In(),
    Out(),
    serviceMock,
    In()
  )

  def path: String = "myPath/hello"

  def serviceLabel: String = "test-service"

  def serviceVersion: String = "1.0"
end ExampleService
