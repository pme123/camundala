package camundala.examples.invoice
package simulation

import camundala.bpmn.CollectEntries
import camundala.examples.invoice.InvoiceReceipt.{ApproveInvoiceUT, ApproverGroup, InvoiceAssignApproverDMN, InvoiceCategory, PrepareBankTransferUT}
import camundala.examples.invoice.ReviewInvoice.{AssignReviewerUT, ReviewInvoiceUT}
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceC7/It/testOnly *InvoiceSimulation
class InvoiceSimulation extends CustomSimulation:

  simulate(
    scenario(ReviewInvoice.example)(
      AssignReviewerUT.example,
      ReviewInvoiceUT.example
    ),
    incidentScenario(
      `Invoice Receipt that fails`,
      "Could not archive invoice..."
    )(
      ApproveInvoiceUT.example
      .waitForSec(1), // tests wait function for UserTasks
      PrepareBankTransferUT.example
    ),
    scenario(InvoiceReceipt.example)(
      waitFor(1),
      ApproveInvoiceUT.example,
      PrepareBankTransferUT.example
    ),
    scenario(WithOverrideScenario)(
      `ApproveInvoiceUT with Override`,
      PrepareBankTransferUT.example
    ),
    scenario(`Invoice Receipt with Review`)(
      NotApproveInvoiceUT,
      subProcess(ReviewInvoice.example)(
        AssignReviewerUT.example,
        ReviewInvoiceUT.example // do clarify
      ),
      ApproveInvoiceUT.example, // now approve
      PrepareBankTransferUT.example
    ),
    scenario(`Invoice Receipt with Review failed`)(
      NotApproveInvoiceUT, // do not approve
      subProcess(`Review Invoice not clarified`)(
        AssignReviewerUT.example,
        ReviewInvoiceNotClarifiedUT // do not clarify
      )
    ),
    scenario(InvoiceAssignApproverDMN.example),
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
    ReviewInvoiceUT.example
      .withOut(ReviewInvoiceUT.Out(false))

  private lazy val NotApproveInvoiceUT =
    ApproveInvoiceUT.example
      .withOut(ApproveInvoiceUT.Out(false))
  // this indirection is needed as we use the same Process for two scenarios (name clash).
  private lazy val `Invoice Receipt with Override` = InvoiceReceipt.example

  private lazy val WithOverrideScenario =
    `Invoice Receipt with Override`
      .exists("approved")
      .notExists("clarified")
      .isEquals("approved", true)
      .isEquals("invoiceCategory", InvoiceCategory.`Travel Expenses`)

  private lazy val `ApproveInvoiceUT with Override` =
    ApproveInvoiceUT.example
      .exists("amount")
      .notExists("amounts")
      .isEquals("amount", 300.0)
  private lazy val `Invoice Receipt that fails` =
    InvoiceReceipt.example
      .withIn(InvoiceReceipt.In(shouldFail = Some(true)))

  private lazy val InvoiceAssignApproverDMN2 =
    InvoiceAssignApproverDMN.example
      .withIn(InvoiceAssignApproverDMN.In(1050, InvoiceCategory.`Travel Expenses`))
      .withOut(
        CollectEntries(Seq(ApproverGroup.accounting, ApproverGroup.sales))
      )

  private lazy val `Review Invoice not clarified` =
    ReviewInvoice.example
      .withOut(ReviewInvoice.Out(false))

  private lazy val `Invoice Receipt with Review` =
    InvoiceReceipt.example
      .withOut(InvoiceReceipt.Out(clarified = Some(true)))

  private lazy val `Invoice Receipt with Review failed` =
    InvoiceReceipt.example
      .withOut(
        InvoiceReceipt.Out(approved = false, clarified = Some(false))
      )
  private lazy val BadValidationP =
    InvoiceReceipt.example
      .withIn(InvoiceReceipt.In(null))