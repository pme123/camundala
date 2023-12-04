package camundala
package examples.invoice
package dmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.dmn.{DmnTesterConfigCreator, DmnTesterStarter}
import camundala.examples.invoice.InvoiceReceipt.InvoiceAssignApproverDMN

object InvoiceDmnTester
    extends DmnTesterConfigCreator,
      DmnTesterStarter,
      BpmnDsl,
      App:

  override protected val projectBasePath: os.Path =
    os.pwd / "05-examples" / "invoice" / "camunda7"

  override val starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig(

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
    given ApiSchema[InvoiceAssignApproverDmnIn] = deriveApiSchema
    given CirceCodec[InvoiceAssignApproverDmnIn] = deriveInOutCodec
  end InvoiceAssignApproverDmnIn

  @description("There are three possible Categories")
  enum InvoiceClassification derives ConfiguredEnumCodec:
    case `day-to-day expense`, budget, exceptional

  object InvoiceClassification:
    given ApiSchema[InvoiceClassification] = deriveEnumSchema

  private lazy val InvoiceAssignApproverDmnUnit =
    collectEntries(
      decisionDefinitionKey = "example-invoice-c7-assignApprover",
      in = InvoiceAssignApproverDmnIn(),
    )

end InvoiceDmnTester
