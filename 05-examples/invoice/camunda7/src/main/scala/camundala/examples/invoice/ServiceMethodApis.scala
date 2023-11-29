package camundala.examples.invoice

import camundala.bpmn.*
import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.domain.*
import sttp.tapir.Schema.annotations.description

object ServiceMethodDeleteApi extends BpmnDsl:

  final val topicName = "service-method-delete"
  type ServiceOut = NoOutput
  lazy val serviceMock = MockedServiceResponse.success204

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1
  )

  object In:
    given ApiSchema[In] = deriveSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
    processStatus: ProcessStatus =  ProcessStatus.succeeded
    )

  object Out:
    given ApiSchema[Out] = deriveSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class Dummy(
      id: Long = 123L,
  )
  object Dummy:
    given ApiSchema[Dummy] = deriveSchema
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

