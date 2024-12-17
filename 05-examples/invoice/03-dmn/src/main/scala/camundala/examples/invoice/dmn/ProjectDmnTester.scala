package camundala
package examples.invoice
package dmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.dmn.*
import bpmn.InvoiceReceipt.InvoiceAssignApproverDMN

object ProjectDmnTester
    extends DmnTesterConfigCreator,
      DmnTesterStarter,
      BpmnProcessDsl,
      App:
  val processName = "NOT USED"
  val descr = ""

  private lazy val localDmnConfigPath: os.Path =
    os.pwd / "05-examples" / "invoice" / "03-dmn" / "src" / "main" / "resources" / "dmnConfigs"

  private lazy val localDmnPath =
    os.pwd / "05-examples" / "invoice" / "04-c7-spring" / "src" / "main" / "resources"

  override def starterConfig: DmnTesterStarterConfig =
    DmnTesterStarterConfig(
      companyName = "camundala",
      dmnPaths = Seq(localDmnPath),
      dmnConfigPaths = Seq(localDmnConfigPath)
    )

  startDmnTester()

  createDmnConfigs(
    InvoiceAssignApproverDMN.example
      .dmnPath("example-invoice-c7-assignApprover")
      .testValues(_.amount, 249, 250, 999, 1000, 1001),
    // for demonstration - created unit test - acceptMissingRules just for demo
    InvoiceAssignApproverDmnUnit
      .acceptMissingRules
      .testUnit
      .dmnPath("example-invoice-c7-assignApprover")
    //  .inTestMode
  )

  case class InvoiceAssignApproverDmnIn(
      invoiceClassification: InvoiceClassification = InvoiceClassification.`day-to-day expense`
  )

  object InvoiceAssignApproverDmnIn:
    given Schema[InvoiceAssignApproverDmnIn] = Schema.derived
    given CirceCodec[InvoiceAssignApproverDmnIn] = deriveCodec
  end InvoiceAssignApproverDmnIn

  @description("There are three possible Categories")
  enum InvoiceClassification:
    case `day-to-day expense`, budget, exceptional

  object InvoiceClassification:
    given Schema[InvoiceClassification] = deriveEnumApiSchema
    given InOutCodec[InvoiceClassification] = deriveEnumInOutCodec

  private lazy val InvoiceAssignApproverDmnUnit =
    collectEntries(
      decisionDefinitionKey = "example-invoice-c7-assignApprover",
      in = InvoiceAssignApproverDmnIn()
    )

end ProjectDmnTester
