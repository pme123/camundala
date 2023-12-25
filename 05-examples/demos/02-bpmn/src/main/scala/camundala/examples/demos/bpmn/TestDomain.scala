package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object TestDomain extends BpmnDsl:

  case class SomeObj(tag: String = "okidoki", isOk: String = "false")
  object SomeObj:
    given ApiSchema[SomeObj] = deriveApiSchema
    given InOutCodec[SomeObj] = deriveCodec
  end SomeObj
  case class ValueWrapper(success: Boolean = false)
  object ValueWrapper:
    given ApiSchema[ValueWrapper] = deriveApiSchema
    given InOutCodec[ValueWrapper] = deriveCodec
  end ValueWrapper
// process In
  case class In(
      name: String = "example",
      optionExample: Option[String] = Some("optionValue"),
      listExample: Seq[String] = List("a", "b"),
      someObj: SomeObj = SomeObj(),
      success: ValueWrapper = ValueWrapper()
  )
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  //  Out
  case class Out(
      success: ValueWrapper = ValueWrapper(),
      //TODO    isBoolean: String = "false",
      successStr: String = "What a CallActivity!",
      optionResult: Option[String] = Some("optionValue"),
      listResult: Seq[String] = List("a", "b")
  )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  // generate-test.bpmn
  val CamundalaGenerateTestPIdent = "camundala-generate-test"
  lazy val CamundalaGenerateTestP = process(
    CamundalaGenerateTestPIdent,
    in = In(),
    out = Out()
  )

end TestDomain
