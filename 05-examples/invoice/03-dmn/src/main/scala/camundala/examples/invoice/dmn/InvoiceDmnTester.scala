package camundala
package examples.invoice
package dmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.dmn.*
import bpmn.InvoiceReceipt.InvoiceAssignApproverDMN

object InvoiceDmnTester
    extends DmnTesterConfigCreator,
      DmnTesterStarter,
      BpmnDsl,
      App:

  override protected val projectBasePath: os.Path =
    os.pwd / "05-examples" / "invoice" / "04-c7-spring"

  private def localDmnPath = os.pwd / "05-examples" / "invoice" / "04-c7-spring" / "src" / "main" / "resources"


  override def starterConfig: DmnTesterStarterConfig =
    DmnTesterStarterConfig(
      dmnPaths = Seq(localDmnPath),
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
                                         invoiceClassification: InvoiceClassification = InvoiceClassification.`day-to-day expense`,
                           )

  object InvoiceAssignApproverDmnIn:
    given Schema[InvoiceAssignApproverDmnIn] = Schema.derived
    given CirceCodec[InvoiceAssignApproverDmnIn] = deriveCodec
  end InvoiceAssignApproverDmnIn

  @description("There are three possible Categories")
  enum InvoiceClassification derives ConfiguredEnumCodec:
    case `day-to-day expense`, budget, exceptional

  object InvoiceClassification:
    given Schema[InvoiceClassification] = deriveEnumSchema

  private lazy val InvoiceAssignApproverDmnUnit =
    collectEntries(
      decisionDefinitionKey = "example-invoice-c7-assignApprover",
      in = InvoiceAssignApproverDmnIn(),
    )

end InvoiceDmnTester
