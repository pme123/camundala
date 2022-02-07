package camundala.examples.demos

import camundala.bpmn.*
import camundala.camunda.GenerateCamundaBpmn
import camundala.domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TestDomain extends BpmnDsl:

  case class SomeObj(tag: String = "okidoki", isOk: String = "false")
  case class ValueWrapper(success: Boolean = false)

// process In
  case class ProcessIn(
      name: String = "example",
      optionExample: Option[String] = Some("optionValue"),
      listExample: Seq[String] = List("a", "b"),
      someObj: SomeObj = SomeObj(),
      success: ValueWrapper = ValueWrapper()
  )
  // process Out
  case class ProcessOut(
      success: ValueWrapper = ValueWrapper(),
  //TODO    isBoolean: String = "false",
      successStr: String = "What a CallActivity!",
      optionResult: Option[String] = Some("optionValue"),
      listResult: Seq[String] = List("a", "b"),
  )

  // call Activity In
  case class CallProcessIn(
      putTag: String = "voila",
      success: ValueWrapper = ValueWrapper(),
      someOption: Option[String] = Some("optionValue"),
      someList: Seq[String] = List("listValue")
  )
  case class CallProcessOut(
      result: String = "What a CallActivity!",
      success: ValueWrapper = ValueWrapper(),
      someOption: Option[String] = Some("optionValue"),
      someList: List[String] = List("optionValue"),
  )

  // generate-test.bpmn
  val CamundalaGenerateTestPIdent = "camundala-generate-test"
  lazy val CamundalaGenerateTestP = process(
    CamundalaGenerateTestPIdent,
    in = ProcessIn(),
    out = ProcessOut()
  )

  val CallProcessCAIdent = "CallProcessCA"
  lazy val CallProcessCA: CallActivity[CallProcessIn, CallProcessOut] =
    callActivity(
      CallProcessCAIdent,
      in = CallProcessIn(),
      out = CallProcessOut(),
      descr = None
    )

end TestDomain
