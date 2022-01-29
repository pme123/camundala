package camundala.examples.demos

import camundala.bpmn.*
import camundala.camunda.GenerateCamundaBpmn
import camundala.domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TestDomain extends BpmnDsl:

  // process In
  case class ProcessIn(name: String = "example", someObj: SomeObj = SomeObj())
  case class SomeObj(tag: String = "okidoki", isOk: Boolean = false)
  // process Out
  case class ProcessOut()//success: Boolean = true, successStr: String = "ok")

  // call Activity In
  case class Complex(putTag: String = "voila")
  case class SomeResult(result: String = "successful?", success: ValueWrapper = ValueWrapper())
  case class ValueWrapper(success: Boolean = false)

  // generate-test.bpmn
  val CamundalaGenerateTestPIdent = "camundala-generate-test"
  lazy val CamundalaGenerateTestP = process(
    CamundalaGenerateTestPIdent,
    in = ProcessIn(),
    out = ProcessOut(),
  )

  val CallProcessCAIdent = "CallProcessCA"
  lazy val CallProcessCA: CallActivity[Complex, SomeResult] = callActivity(
    CallProcessCAIdent,
    in = Complex(),
    out = SomeResult(),
    descr = None
  )

end TestDomain
