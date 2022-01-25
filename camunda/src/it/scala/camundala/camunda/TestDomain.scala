package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*
import camundala.camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TestDomain extends BpmnDsl:

  // process In
  case class ProcessIn(name: String = "example", someObj: SomeObj = SomeObj())
  case class SomeObj(tag: String = "okidoki", isOk: Boolean = false)
  // process Out
  case class ProcessOut(success: Boolean = true, successStr: String = "ok")

  // call Activity In
  case class Complex(putTag: String = "voila")
  case class SomeResult(result: String = "successful?", success: ValueWrapper = ValueWrapper())
  case class ValueWrapper(success: Boolean = false)

  // generate-test.bpmn
  val CamundalaGenerateTestPIdent = "CamundalaGenerateTestP"
  lazy val CamundalaGenerateTestP = process(
    CamundalaGenerateTestPIdent,
    in = ProcessIn(),
    out = ProcessOut(),
  )

  val DoSomethingUTIdent = "DoSomethingUT"
  lazy val DoSomethingUT = userTask(
    DoSomethingUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val CallProcessCAIdent = "CallProcessCA"
  lazy val CallProcessCA: CallActivity[Complex, SomeResult] = callActivity(
    CallProcessCAIdent,
    in = Complex(),
    out = SomeResult(),
    descr = None
  )

  val Event1rjp2nkIdent = "Event_1rjp2nk"
  lazy val Event1rjp2nk = endEvent(
    Event1rjp2nkIdent,
    descr = None
  )

  val Event1ni9jkvIdent = "Event_1ni9jkv"
  lazy val Event1ni9jkv = endEvent(
    Event1ni9jkvIdent,
    descr = None
  )

end TestDomain
