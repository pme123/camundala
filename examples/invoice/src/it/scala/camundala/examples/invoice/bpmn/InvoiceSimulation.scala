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
    processScenario(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    processScenario(`Invoice Receipt`)(
      ApproveInvoiceUT,
      PrepareBankTransferUT
    ),
    processScenario(`Invoice Receipt with Review`)(
      ApproveInvoiceUT
        .withOut(ApproveInvoice(false)), // do not approve
      subProcess(`Review Invoice clarified`)(
        AssignReviewerUT,
        ReviewInvoiceUT // do clarify
      ),
      ApproveInvoiceUT, // now approve
      PrepareBankTransferUT
    ),
    processScenario(`Invoice Receipt with Review failed`)(
      ApproveInvoiceUT
        .withOut(ApproveInvoice(false)), // do not approve
      subProcess(`Review Invoice not clarified`)(
        AssignReviewerUT,
        ReviewInvoiceUT.withOut(InvoiceReviewed(false)) // do not clarify
      )
    ),
    processScenario("Bad Validation")(
      BadValidationP
        .start("Bad Validation", 500)
    )
  )
}
