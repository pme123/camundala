package camundala.examples.invoice.bpmn

import camundala.domain.GenericExternalTask.ProcessStatus
import camundala.domain.*

trait ServiceMethodServices extends BpmnServiceTaskDsl:
  val serviceLabel: String   = "TEST SERVICES for Service Methods"
  val serviceVersion: String = "0.1"
end ServiceMethodServices

object ServiceMethodDeleteApi extends ServiceMethodServices:

  final val topicName = "service-method-delete"
  val descr           = "Delete Dummy - mocking test"
  val path: String    = "DELETE /services/method"

  type ServiceIn  = NoInput
  type ServiceOut = NoOutput
  lazy val serviceInExample = NoInput()
  lazy val serviceMock      = MockedServiceResponse.success204

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1
  )

  object In:
    given ApiSchema[In]  = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
      processStatus: ProcessStatus = ProcessStatus.succeeded
  )

  object Out:
    given ApiSchema[Out]  = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class Dummy(
      id: Long = 123L
  )
  object Dummy:
    given ApiSchema[Dummy]  = deriveApiSchema
    given InOutCodec[Dummy] = deriveCodec
  end Dummy

  final lazy val example: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    serviceTask(
      in = In(),
      out = Out(),
      defaultServiceOutMock = serviceMock,
      serviceInExample = serviceInExample
    )

end ServiceMethodDeleteApi

object ServiceMethodListApi extends ServiceMethodServices:

  final val topicName = "service-method-list"
  val descr           = "Delete Dummy - mocking test"
  val path: String    = "GET /services/method"
  type ServiceIn  = NoInput
  type ServiceOut = List[Dummy]
  lazy val serviceInExample = NoInput()
  lazy val serviceMock      = MockedServiceResponse.success200(List(Dummy()))

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1
  )

  object In:
    given ApiSchema[In]  = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
      processStatus: ProcessStatus = ProcessStatus.succeeded,
      dummies: Seq[Dummy] = Seq(Dummy())
  )

  object Out:
    given ApiSchema[Out]  = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class Dummy(
      id: Long = 123L
  )
  object Dummy:
    given ApiSchema[Dummy]  = deriveApiSchema
    given InOutCodec[Dummy] = deriveCodec
  end Dummy

  final lazy val example: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    serviceTask(
      in = In(),
      out = Out(),
      defaultServiceOutMock = serviceMock,
      serviceInExample = serviceInExample
    )

end ServiceMethodListApi
