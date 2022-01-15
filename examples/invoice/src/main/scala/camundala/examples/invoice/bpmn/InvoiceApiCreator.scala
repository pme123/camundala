package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import InvoiceApi.*

object InvoiceApiCreator extends APICreator {

  def title = "Invoice Example Process API"

  def version = "1.0"

  override lazy val serverPort = 8034
  
  override def basePath: Path = pwd / "examples" / "invoice"


  apiEndpoints(
      invoiceReceiptProcess
        .endpoints(
          invoiceAssignApproverDMN2.endpoint,
          approveInvoiceUT.endpoint
            .withOutExample("Invoice approved", ApproveInvoice())
            .withOutExample("Invoice NOT approved", ApproveInvoice(false)),
          prepareBankTransferUT.endpoint
        ),
      reviewInvoiceProcess.endpoints(
        assignReviewerUT.endpoint,
        reviewInvoiceUT.endpoint
          .withOutExample("Invoice clarified", InvoiceReviewed())
          .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))
      )
    )

}
