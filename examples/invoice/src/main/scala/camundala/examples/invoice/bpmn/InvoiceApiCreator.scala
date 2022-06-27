package camundala.examples.invoice.bpmn

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object InvoiceApiCreator extends ApiCreator:

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "invoice")
      .withPort(8034)
      .withCawemoFolder("a76e4b8e-8631-4d20-a8eb-258b000ff88a--camundala")

  protected val title = "Invoice Example Process API"

  protected val version = "1.0"

  document {
    api(`Invoice Receipt`)(
      InvoiceAssignApproverDMN,
      ApproveInvoiceUT,
      PrepareBankTransferUT
    )
    api(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    )
  }

  private lazy val ApproveInvoiceUT =
    InvoiceApi.ApproveInvoiceUT
      .withOutExample("Invoice approved", ApproveInvoice())
      .withOutExample("Invoice NOT approved", ApproveInvoice(false))

  private lazy val ReviewInvoiceUT =
    InvoiceApi.ReviewInvoiceUT
      .withOutExample("Invoice clarified", InvoiceReviewed())
      .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))
