package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

object EnumExample extends BpmnDsl:

  enum Input derives Adt.Decoder, Adt.Encoder:

    case A(
        someValue: Option[String] = Some("hello"),
        simpleEnum: SimpleEnum = SimpleEnum.One,
        customMock: Option[Output] = Some(Output.A())
    )
  object Input:
    given Schema[Input] = Schema.derived

  enum Output derives Adt.Decoder, Adt.Encoder:

    case A(intValue: Int = 12, simpleEnum: SimpleEnum = SimpleEnum.One)
  object Output:
    given Schema[Output] = Schema.derived

  enum SimpleEnum derives Adt.PureDecoder, Adt.PureEncoder:
     case One,Two
  object SimpleEnum:
    given Schema[SimpleEnum] = Schema.derived

  lazy val enumExample = process(
    "enum-example",
    in = Input.A(),
    out = Output.A()
  )

end EnumExample