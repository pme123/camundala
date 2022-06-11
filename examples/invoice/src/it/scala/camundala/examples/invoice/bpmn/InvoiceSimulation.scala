package camundala
package examples.invoice.bpmn

import camundala.examples.invoice.bpmn.InvoiceApi.*
import bpmn.*
import domain.*
import simulation.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleInvoice/GatlingIt/testOnly *InvoiceSimulation
class InvoiceSimulation extends SimulationDsl:

  override implicit def config: SimulationConfig =
    super.config
      .withPort(8034)
  //.withUserAtOnce(100) // do load testing
  val ReviewInvoiceNotClarifiedUT = ReviewInvoiceUT.withOut(InvoiceReviewed(false))

  // this indirection is needed as we use the same Process for two scenarios (name clash).
  val `Invoice Receipt with Override` = `Invoice Receipt`
  val WithOverrideScenario = `Invoice Receipt with Override`
    .exists("approved")
    .notExists("clarified")
    .isEquals("approved", true)

  lazy val `ApproveInvoiceUT with Override` =
    ApproveInvoiceUT
      .exists("amount")
      .notExists("amounts")
      .isEquals("amount", 300.0)

  simulate {
     scenario(`Review Invoice`) (
       AssignReviewerUT,
       ReviewInvoiceUT
     )
     scenario(`Invoice Receipt`) (
       ApproveInvoiceUT,
       PrepareBankTransferUT,
     )
     scenario(WithOverrideScenario) (
       `ApproveInvoiceUT with Override`,
       PrepareBankTransferUT,
     )
     scenario(`Invoice Receipt with Review`)(
       ApproveInvoiceUT
         .withOut(ApproveInvoice(false)), // do not approve
       subProcess(`Review Invoice`)(
         AssignReviewerUT,
         ReviewInvoiceUT // do clarify
       ),
       ApproveInvoiceUT, // now approve
       PrepareBankTransferUT
     )
     scenario(`Invoice Receipt with Review failed`)(
       ApproveInvoiceUT
         .withOut(ApproveInvoice(false)), // do not approve
       subProcess(`Review Invoice not clarified`)(
         AssignReviewerUT,
         ReviewInvoiceNotClarifiedUT // do not clarify
       )
     )
     badScenario(BadValidationP, 500, Some("Validation Error: Input is not valid: DecodingFailure(Attempt to decode value on failed cursor, List(DownField(creditor)))"))

  }

