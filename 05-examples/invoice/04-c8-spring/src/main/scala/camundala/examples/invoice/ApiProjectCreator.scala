package camundala.examples.invoice

import domain.*
import bpmn.*

import camundala.api.*
import camundala.bpmn.*

object ApiProjectCreator extends DefaultApiCreator:

  val projectName = "invoice-example"

  protected val title = "Invoice Example Process API"

  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(os.pwd / "05-examples" / "invoice" / "camunda8")
      .withPort(8034)
      .withCawemoFolder("a76e4b8e-8631-4d20-a8eb-258b000ff88a--camundala")

  document (
    api(`Invoice Receipt`)(
      InvoiceAssignApproverDMN,
      ApproveInvoiceUT,
      PrepareBankTransferUT
    ),
    api(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    group("DMNs")(
      InvoiceAssignApproverDMN
    ),
  )

  private lazy val ApproveInvoiceUT =
    bpmn.ApproveInvoiceUT
      .withOutExample("Invoice approved", ApproveInvoice())
      .withOutExample("Invoice NOT approved", ApproveInvoice(false))

  private lazy val ReviewInvoiceUT =
    bpmn.ReviewInvoiceUT
      .withOutExample("Invoice clarified", InvoiceReviewed())
      .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))
