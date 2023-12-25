package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object EnumExample extends BpmnDsl:

  enum Input:

    case A(
        someValue: Option[String] = Some("hello"),
        simpleEnum: SimpleEnum = SimpleEnum.One,
        customMock: Option[Output] = Some(Output.A())
    )
  end Input
  object Input:
    given ApiSchema[Input] = deriveApiSchema
    given InOutCodec[Input] = deriveInOutCodec

  enum Output:

    case A(
        someOut: Option[String] = Some("hello"),
        intValue: Int = 12,
        simpleEnum: SimpleEnum = SimpleEnum.One
    )
  end Output
  object Output:
    given ApiSchema[Output] = deriveApiSchema
    given InOutCodec[Output] = deriveInOutCodec

  enum SimpleEnum:
    case One, Two

  object SimpleEnum:
    given ApiSchema[SimpleEnum] = deriveEnumApiSchema
    given InOutCodec[SimpleEnum] = deriveEnumInOutCodec

  lazy val enumExample = process(
    "enum-example",
    in = Input.A(),
    out = Output.A()
  )

end EnumExample
