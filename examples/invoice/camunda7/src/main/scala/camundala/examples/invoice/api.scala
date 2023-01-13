// put your api in a package of your project
package camundala.examples.invoice

// import camundala dependencies
import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*

// import the projects bpmns (Processes, UserTasks etc.)
import camundala.examples.invoice.bpmn.*
// import the projects domain (inputs and outputs)
// - needed if you have additional infos (e.g.not needed for simulation)
import camundala.examples.invoice.domain.*

// define an object that extends from a common Api Creator
object api extends DefaultApiCreator:
  // technical name of the project
  val projectName = "invoice-example"
  // readable name of the project
  protected val title = "Invoice Example Process API"
  // version of your project
  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "invoice" / "camunda7")
      .withPort(8034)
      .withCawemoFolder("a76e4b8e-8631-4d20-a8eb-258b000ff88a--camundala")

  document(
    api(`Invoice Receipt`)(
      InvoiceAssignApproverDMN2,
      ApproveInvoiceUT,
      PrepareBankTransferUT
    ),
    api(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    group("User Tasks")(
      api(ApproveInvoiceUT), // api( is optional
      PrepareBankTransferUT,
      AssignReviewerUT,
      ReviewInvoiceUT
    ),
    api(InvoiceAssignApproverDMN3) // api( is optional
  )

  private lazy val ApproveInvoiceUT =
    bpmn.ApproveInvoiceUT
      .withOutExample("Invoice approved", ApproveInvoice())
      .withOutExample("Invoice NOT approved", ApproveInvoice(false))

  private lazy val ReviewInvoiceUT =
    bpmn.ReviewInvoiceUT
      .withOutExample("Invoice clarified", InvoiceReviewed())
      .withOutExample("Invoice NOT clarified", InvoiceReviewed(false))

  private lazy val InvoiceAssignApproverDMN1 =
    bpmn.InvoiceAssignApproverDMN
      .withInExample(budget)
      .withInExample(`day-to-day expense`)
      .withInExample(exceptional)

  val budget = SelectApproverGroup()
  val `day-to-day expense` = SelectApproverGroup(125, InvoiceCategory.Misc)
  val exceptional = SelectApproverGroup(12345, InvoiceCategory.Misc)

  private lazy val InvoiceAssignApproverDMN2 =
    bpmn.InvoiceAssignApproverDMN
      .withOutExample("budget", CollectEntries(ApproverGroup.management))
      .withOutExample(
        "day-to-day expense",
        CollectEntries(ApproverGroup.accounting, ApproverGroup.sales)
      )

  private lazy val InvoiceAssignApproverDMN3 =
    bpmn.InvoiceAssignApproverDMN
      .withExample(
        "budget",
        bpmn.InvoiceAssignApproverDMN
          .withIn(SelectApproverGroup())
          .withOut(Seq(ApproverGroup.management))
      )
      .withExample(
        "day-to-day expense",
        bpmn.InvoiceAssignApproverDMN
          .withIn(SelectApproverGroup(125, InvoiceCategory.Misc))
          .withOut(
            Seq(ApproverGroup.accounting, ApproverGroup.sales)
          )
      )
