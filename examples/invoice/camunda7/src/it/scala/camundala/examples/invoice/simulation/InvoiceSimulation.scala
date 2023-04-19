package camundala.examples.invoice
package simulation

import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.domain.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceC7/It/testOnly *InvoiceSimulation
class InvoiceSimulation extends CustomSimulation:

  simulate(
    scenario(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    incidentScenario(
      `Invoice Receipt that fails`,
      "Could not archive invoice..."
    )(
      ApproveInvoiceUT
      .waitForSec(1), // tests wait function for UserTasks
      PrepareBankTransferUT
    ),
    scenario(`Invoice Receipt`)(
      waitFor(1),
      ApproveInvoiceUT,
      PrepareBankTransferUT
    ),
    scenario(WithOverrideScenario)(
      `ApproveInvoiceUT with Override`,
      PrepareBankTransferUT
    ),
    scenario(`Invoice Receipt with Review`)(
      NotApproveInvoiceUT,
      subProcess(`Review Invoice`)(
        AssignReviewerUT,
        ReviewInvoiceUT // do clarify
      ),
      ApproveInvoiceUT, // now approve
      PrepareBankTransferUT
    ),
    scenario(`Invoice Receipt with Review failed`)(
      NotApproveInvoiceUT, // do not approve
      subProcess(`Review Invoice not clarified`)(
        AssignReviewerUT,
        ReviewInvoiceNotClarifiedUT // do not clarify
      )
    ),
    scenario(InvoiceAssignApproverDMN),
    scenario(InvoiceAssignApproverDMN2),
    badScenario(
      BadValidationP,
      500,
      "Validation Error: Input is not valid: DecodingFailure at .creditor: Missing required field"
    )
  )

  override implicit def config =
    super.config
      .withPort(8034)
  //.withUserAtOnce(100) // do load testing

  private lazy val ReviewInvoiceNotClarifiedUT =
    ReviewInvoiceUT
      .withOut(InvoiceReviewed(false))

  private lazy val NotApproveInvoiceUT =
    ApproveInvoiceUT
      .withOut(ApproveInvoice(false))
  // this indirection is needed as we use the same Process for two scenarios (name clash).
  private lazy val `Invoice Receipt with Override` = `Invoice Receipt`

  private lazy val WithOverrideScenario =
    `Invoice Receipt with Override`
      .exists("approved")
      .notExists("clarified")
      .isEquals("approved", true)
      .isEquals("invoiceCategory", InvoiceCategory.`Travel Expenses`)

  private lazy val `ApproveInvoiceUT with Override` =
    ApproveInvoiceUT
      .exists("amount")
      .notExists("amounts")
      .isEquals("amount", 300.0)
  private lazy val `Invoice Receipt that fails` =
    `Invoice Receipt`
      .withIn(InvoiceReceipt(shouldFail = Some(true)))
