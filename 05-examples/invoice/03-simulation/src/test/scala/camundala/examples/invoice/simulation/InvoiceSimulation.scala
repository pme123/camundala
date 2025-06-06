package camundala.examples.invoice
package simulation

import camundala.domain.{CollectEntries, ErrorCodes}
import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceReceipt.*
import camundala.examples.invoice.bpmn.ReviewInvoice.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceSimulation/test
// exampleInvoiceSimulation/testOnly *InvoiceSimulation
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
    scenario(`Invoice Receipt with Review mocked`)(
      NotApproveInvoiceUT,
      // subProcess Mocked - so nothing to do
      ApproveInvoiceUT, // now approve
      PrepareBankTransferUT
    ),
    scenario(InvoiceAssignApproverDMN),
    scenario(InvoiceAssignApproverDMN2),
    // mocking
    scenario(`Invoice Receipt mocked invoiceReviewed`)(
      NotApproveInvoiceUT,
      // subProcess not needed because of mocking
      ApproveInvoiceUT, // now approve
      PrepareBankTransferUT
    ),
    scenario(`Review Invoice mocked`), // mocks itself
    // ExternalTask - works only if exampleInvoiceWorkerC7 is running
    scenario(
      `Archive Invoice`
    ),
    scenario(
      `Archive Invoice handled`
    ),
    scenario(
      `Archive Invoice handled regex matched`
    ),
    incidentScenario(
      `Archive Invoice handled not matched`,
      "The error was handled, but did not match the defined 'regexHandledErrors'."
    ),
    incidentScenario(
      `Archive Invoice that fails`,
      "Could not archive invoice"
    ),
    scenario(`Archive Invoice mocked output`)
  )

  override implicit def config =
    super.config
      .withPort(8034)
    // .withMaxCount(20)
    // .withLogLevel(LogLevel.DEBUG)

  private lazy val `Invoice Receipt` = InvoiceReceipt.example
  private lazy val `Invoice Receipt mocked invoiceReviewed` =
    `Invoice Receipt with Review`
      .withIn(InvoiceReceipt.In(inConfig =
        Some(InvoiceReceipt.InConfig(
          invoiceReviewedMock = Some(ReviewInvoice.Out())
        ))
      ))

  private lazy val ApproveInvoiceUT = InvoiceReceipt.ApproveInvoiceUT.example
  private lazy val PrepareBankTransferUT =
    InvoiceReceipt.PrepareBankTransferUT.example

  private lazy val `Review Invoice` = ReviewInvoice.example
  private lazy val `Review Invoice mocked` = ReviewInvoice.example
    .mockWith(ReviewInvoice.Out())

  private lazy val AssignReviewerUT = ReviewInvoice.AssignReviewerUT.example
  private lazy val ReviewInvoiceUT = ReviewInvoice.ReviewInvoiceUT.example

  private lazy val ReviewInvoiceNotClarifiedUT =
    ReviewInvoiceUT
      .withOut(ReviewInvoice.ReviewInvoiceUT.Out(false))

  private lazy val NotApproveInvoiceUT =
    ApproveInvoiceUT
      .withOut(InvoiceReceipt.ApproveInvoiceUT.Out(false))
  // this indirection is needed as we use the same Process for two scenarios (name clash).
  private lazy val `Invoice Receipt with Override` = InvoiceReceipt.example

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
    InvoiceReceipt.example
      .withIn(InvoiceReceipt.In(inConfig = Some(InvoiceReceipt.InConfig(shouldFail = Some(true)))))

  private lazy val InvoiceAssignApproverDMN =
    InvoiceReceipt.InvoiceAssignApproverDMN.example
  private lazy val InvoiceAssignApproverDMN2 =
    InvoiceAssignApproverDMN
      .withIn(
        InvoiceReceipt.InvoiceAssignApproverDMN
          .In(1050, InvoiceCategory.`Travel Expenses`)
      )
      .withOut(
        CollectEntries(Seq(ApproverGroup.accounting, ApproverGroup.sales))
      )

  private lazy val `Review Invoice not clarified` =
    ReviewInvoice.example
      .withOut(ReviewInvoice.Out(false))

  private lazy val `Invoice Receipt with Review` =
    InvoiceReceipt.example
      .withOut(InvoiceReceipt.Out(clarified = Some(true)))

  private lazy val `Invoice Receipt with Review mocked` =
    InvoiceReceipt.example
      .withOut(InvoiceReceipt.Out(clarified = Some(true)))
      .mockWorker(ReviewInvoice.processName)

  private lazy val `Invoice Receipt with Review failed` =
    InvoiceReceipt.example
      .withOut(
        InvoiceReceipt.Out(approved = false, clarified = Some(false), archived = None)
      )

  private lazy val `Archive Invoice` =
    ArchiveInvoice.example
      .withIn(_.copy(shouldFail = Some(false)))

  private lazy val `Archive Invoice handled` =
    ArchiveInvoice.example
      .withOut(_.copy(archived = None))
      .handleError(ErrorCodes.`custom-run-error`)

  private lazy val `Archive Invoice handled not matched` =
    ArchiveInvoice.example
      .handleError(ErrorCodes.`custom-run-error`)
      .handleErrorWithRegex("Some other error msg")

  private lazy val `Archive Invoice handled regex matched` =
    ArchiveInvoice.example
      .withOut(_.copy(archived = None))
      .handleError(ErrorCodes.`custom-run-error`)
      .handleErrorWithRegex("Could not .* invoice")

  private lazy val `Archive Invoice that fails` =
    ArchiveInvoice.example

  private lazy val `Archive Invoice mocked output` =
    ArchiveInvoice.example
      .mockWith(ArchiveInvoice.Out(Some(true)))

end InvoiceSimulation
