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

  override implicit def config: SimulationConfig =
    super.config
      .withPort(8034)
      //.withUserAtOnce(100) // do load testing


  simulate(
    processScenario(ReviewInvoiceP)(
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    processScenario(InvoiceReceiptP)(
      ApproveInvoiceUT,
      PrepareBankTransferUT
    ),
    processScenario(InvoiceReceiptWithReviewP)(
      ApproveInvoiceUT
        .withOut(ApproveInvoice(false)), // do not approve
      InvoiceReceiptP
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      AssignReviewerUT,
      ReviewInvoiceUT,
      ReviewInvoiceP.check(), // check if sub process successful
      InvoiceReceiptP.switchToMainProcess(),
      ApproveInvoiceUT, // now approve
      PrepareBankTransferUT
    ),
    processScenario(InvoiceReceiptWithReviewFailedP)(
      ApproveInvoiceUT
        .withOut(ApproveInvoice(false)), // do not approve
      InvoiceReceiptP
        .switchToCalledProcess(), // switch to Review Process (Call Activity)
      AssignReviewerUT,
      ReviewInvoiceUT.withOut(InvoiceReviewed(false)),
      ReviewInvoiceP
        .withOut(InvoiceReviewed(false))
        .check(), // check if sub process successful
      InvoiceReceiptP.switchToMainProcess()
    ),
    processScenario("Bad Validation")(
      InvoiceReceiptP
        .withIn(InvoiceReceipt(null))
        .start("Bad Validation", 500)
    )
  )
}
