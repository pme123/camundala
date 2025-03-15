package camundala.examples.demos.bpmn

import camundala.domain.*

object TestDomain extends BpmnProcessDsl:

  lazy val processName: String = "simulation-TestOverrides"
  lazy val descr: String = ""

  case class SomeObj(tag: String = "okidoki", isOk: String = "false")
  object SomeObj:
    given ApiSchema[SomeObj] = deriveApiSchema
    given InOutCodec[SomeObj] = deriveInOutCodec
  end SomeObj
  case class ValueWrapper(success: Boolean = false)
  object ValueWrapper:
    given ApiSchema[ValueWrapper] = deriveApiSchema
    given InOutCodec[ValueWrapper] = deriveInOutCodec
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
    given InOutCodec[In] = deriveInOutCodec
  end In

  //  Out
  case class Out(
      success: ValueWrapper = ValueWrapper(),
      optionResult: Option[String] = Some("optionValue"),
      listResult: Seq[String] = List("a", "b")
  )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  end Out

  // generate-test.bpmn
  lazy val CamundalaGenerateTestP = process(
    in = In(),
    out = Out()
  )

end TestDomain
