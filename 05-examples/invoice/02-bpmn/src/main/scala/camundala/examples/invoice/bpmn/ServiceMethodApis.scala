package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.domain.*

object ServiceMethodDeleteApi extends BpmnDsl:

  final val topicName = "service-method-delete"
  type ServiceOut = NoOutput
  lazy val serviceMock = MockedServiceResponse.success204

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
                 id: Int = 1
               )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
                  processStatus: ProcessStatus =  ProcessStatus.succeeded
                )

  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class Dummy(
                    id: Long = 123L,
                  )
  object Dummy:
    given ApiSchema[Dummy] = deriveApiSchema
    given InOutCodec[Dummy] = deriveCodec
  end Dummy

  final lazy val example: ServiceTask[In, Out, ServiceOut] =
    serviceTask(
      topicName,
      descr = "Delete Dummy - mocking test",
      in = In(),
      out = Out(),
      defaultServiceOutMock = serviceMock,
    )

end ServiceMethodDeleteApi

object ServiceMethodListApi extends BpmnDsl:

  final val topicName = "service-method-list"
  type ServiceOut = List[Dummy]
  lazy val serviceMock = MockedServiceResponse.success200(List(Dummy()))

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
                 id: Int = 1
               )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
                  processStatus: ProcessStatus =  ProcessStatus.succeeded,
                  dummies: Seq[Dummy] = Seq(Dummy())
                )

  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class Dummy(
                    id: Long = 123L,
                  )
  object Dummy:
    given ApiSchema[Dummy] = deriveApiSchema
    given InOutCodec[Dummy] = deriveCodec
  end Dummy

  final lazy val example: ServiceTask[In, Out, ServiceOut] =
    serviceTask(
      topicName,
      descr = "Delete Dummy - mocking test",
      in = In(),
      out = Out(),
      defaultServiceOutMock = serviceMock,
    )

end ServiceMethodListApi

