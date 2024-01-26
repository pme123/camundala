package camundala.examples.invoice

import domain.*
import bpmn.*

import camundala.api.*
import camundala.bpmn.*

object ApiProjectCreator extends DefaultApiCreator:
  lazy val companyName = "MyCompany"

  val projectName = "invoice-example"

  protected val title = "Invoice Example Process API"

  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(os.pwd / "05-examples" / "invoice" / "camunda8")
      .withPort(8034)

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
