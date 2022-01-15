package camundala
package examples.invoice.bpmn

import camundala.examples.invoice.bpmn.InvoiceApi.*
import api.*
import bpmn.*
import domain.*
import gatling.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleInvoice/GatlingIt/testOnly *InvoiceSimulation
class InvoiceSimulation extends SimulationRunner {

  override val serverPort = 8034

  simulate(
    processScenario("Review Invoice")(
      reviewInvoiceProcess,
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.getAndComplete(),
    ),
    processScenario("Invoice Receipt")(
      invoiceReceiptProcess,
      approveInvoiceUT.getAndComplete(),
      prepareBankTransferUT.getAndComplete(),
    ),
    processScenario("Invoice Receipt with Review")(
      invoiceReceiptProcess
        .withOut(InvoiceReceiptCheck(clarified = Some(true))),
      approveInvoiceUT
        .withOut(ApproveInvoice(false))
        .getAndComplete(), // do not approve
      invoiceReceiptProcess
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.getAndComplete(),
      reviewInvoiceProcess.check(), // check if sub process successful
      invoiceReceiptProcess.switchToMainProcess(),
      approveInvoiceUT.getAndComplete(), // now approve
      prepareBankTransferUT.getAndComplete()
    ),
    processScenario("Invoice Receipt with Review failed")(
      invoiceReceiptProcess
        .withOut(InvoiceReceiptCheck(approved = false, clarified = Some(false))),
      approveInvoiceUT
        .withOut(ApproveInvoice(false))
        .getAndComplete(), // do not approve
      invoiceReceiptProcess
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      assignReviewerUT.getAndComplete(),
      reviewInvoiceUT.withOut(InvoiceReviewed(false)).getAndComplete(),
      reviewInvoiceProcess
        .withOut(InvoiceReviewed(false))
        .check(), // check if sub process successful
      invoiceReceiptProcess.switchToMainProcess(),
    )
  )
}
