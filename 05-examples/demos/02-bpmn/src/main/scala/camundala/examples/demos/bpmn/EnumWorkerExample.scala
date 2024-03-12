package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object EnumWorkerExample extends BpmnServiceWorkerDsl:

  val serviceLabel: String = "Demo for Enum as Inputs and Outputs in Worker"
  val serviceVersion: String = "1.4"
  val topicName: String = "myEnumWorkerTopic"

  val descr = ""
  val path: String = "GET /people"

  type ServiceIn = NoInput
  type ServiceOut = NoOutput
  lazy val serviceInExample = NoInput()
  lazy val serviceMock: MockedServiceResponse[ServiceOut] =
    MockedServiceResponse.success200(NoOutput())

  enum In:
    case A(
        someValue: Option[String] = Some("hello"),
        simpleEnum: SimpleEnum = SimpleEnum.One,
        customMock: Option[Out] = Some(Out.A()),
        @description(typeDescription(A))
        `type`: String = enumType(A)
    )
    case B(
        otherValue: String = "other",
        @description(typeDescription(B))
        `type`: String = enumType(B)
    )
  end In
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec

  enum Out:

    case A(
        someOut: Option[String] = Some("hello"),
        intValue: Int = 12,
        simpleEnum: SimpleEnum = SimpleEnum.One
    )
    case B(
        otherOut: Option[String] = Some("other")
    )
  end Out
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec

  enum SimpleEnum:
    case One, Two

  object SimpleEnum:
    given ApiSchema[SimpleEnum] = deriveEnumApiSchema
    given InOutCodec[SimpleEnum] = deriveEnumInOutCodec

  lazy val example = serviceTask(
    in = In.A(),
    out = Out.A(),
    serviceMock,
    serviceInExample
  ).withEnumInExample(In.B())
    .withEnumOutExample(Out.B())

  lazy val exampleB = serviceTask(
    in = In.B(),
    out = Out.B(),
    serviceMock,
    serviceInExample
  )

end EnumWorkerExample
