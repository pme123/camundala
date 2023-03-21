package camundala
package examples.invoice
package dmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.dmn.{DmnConfigWriter, DmnTesterConfigCreator, DmnTesterStarter}
import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.domain.*

object InvoiceDmnTesterConfigCreator
    extends DmnTesterConfigCreator,
      DmnConfigWriter,
      DmnTesterStarter,
      App:

  override protected val projectBasePath: Path =
    pwd / "examples" / "invoice" / "camunda7"

  override val starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig(
  )

  startDmnTester()

  createDmnConfigs(
    InvoiceAssignApproverDMN
      .dmnPath("invoiceBusinessDecisions")
      .testValues("amount", 249, 250, 999, 1000, 1001),
    // for demonstration - created unit test - acceptMissingRules just for demo
    InvoiceAssignApproverDmnUnit
      .acceptMissingRules
      .testUnit
      .dmnPath("invoiceBusinessDecisions")
      .inTestMode
  )

  case class InvoiceAssignApproverDmnIn(
                                         invoiceClassification: InvoiceClassification = InvoiceClassification.`day-to-day expense`,
                           )

  object InvoiceAssignApproverDmnIn:
    given Schema[InvoiceAssignApproverDmnIn] = Schema.derived
    given Encoder[InvoiceAssignApproverDmnIn] = deriveEncoder
    given Decoder[InvoiceAssignApproverDmnIn] = deriveDecoder
  end InvoiceAssignApproverDmnIn

  @description("There are three possible Categories")
  enum InvoiceClassification derives ConfiguredEnumCodec:
    case `day-to-day expense`, budget, exceptional

  object InvoiceClassification:
    given Schema[InvoiceClassification] = Schema.derived

  private lazy val InvoiceAssignApproverDmnUnit =
    collectEntries(
      decisionDefinitionKey = "invoice-assign-approver",
      in = InvoiceAssignApproverDmnIn(),
    )

end InvoiceDmnTesterConfigCreator
