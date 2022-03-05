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

  override val cawemoFolder = Some("a76e4b8e-8631-4d20-a8eb-258b000ff88a--camundala")

  override lazy val serverPort = 8034

  override def basePath: Path = pwd / "examples" / "invoice"

  apiEndpoints(
    InvoiceReceiptP
      .endpoints(
        InvoiceAssignApproverDMN2,
        ApproveInvoiceUT
          .withOutExample("Invoice approved", ApproveInvoice())
          .withOutExample("Invoice NOT approved", ApproveInvoice(false)),
        PrepareBankTransferUT
      ),
    ReviewInvoiceP.endpoints(
      AssignReviewerUT,
      ReviewInvoiceUT
        .withOutExample("Invoice clarified", InvoiceReviewed())
        .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))
    )
  )

}
