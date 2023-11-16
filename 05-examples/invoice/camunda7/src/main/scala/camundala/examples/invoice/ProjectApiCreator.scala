// put your api in a package of your project
package camundala.examples.invoice

// import camundala dependencies
import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.InvoiceReceipt.InvoiceAssignApproverDMN.*
import camundala.examples.invoice.InvoiceReceipt.{ApproverGroup, InvoiceAssignApproverDMN, PrepareBankTransferUT}

// define an object that extends from a common Api Creator
// exampleInvoiceC7/run
object ProjectApiCreator extends DefaultApiCreator:
  // technical name of the project
  val projectName = "invoice-example"
  // readable name of the project
  protected val title = "Invoice Example Process API"
  // version of your project
  protected val version = "1.0"

  document(
    api(InvoiceReceipt.example)(
      InvoiceAssignApproverDMN1,
      ApproveInvoiceUT,
      InvoiceReceipt.PrepareBankTransferUT.example
    ),
    api(ReviewInvoice.example)(
      ReviewInvoice.AssignReviewerUT.example,
      ReviewInvoiceUT
    ),
    group("Workers")(
      StarWarsRestApi.example,
      api(ArchiveInvoice.example)
    ),
    group("User Tasks")(
      api(ApproveInvoiceUT), // api( is optional
      InvoiceReceipt.PrepareBankTransferUT.example,
      ReviewInvoice.AssignReviewerUT.example,
      ReviewInvoiceUT
    ),
    group("DMNs")(
      api(InvoiceAssignApproverDMN2) // api( is optional)
      //InvoiceAssignApproverDMN3 // want be shown as only one DMN with the same id is shown in the API.
    )
  )

  override protected lazy val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(os.pwd / "examples" / "invoice" / "camunda7")
      .withDocProjectUrl(project =>
        s"https://webstor.ch/camundala/myCompany/$project"
      )
      .withPort(8034)
      .withDiagramDownloadPath("diagrams")
//  .withCawemoFolder("a76e4b8e-8631-4d20-a8eb-258b000ff88a--camundala")


  private lazy val ApproveInvoiceUT =
    InvoiceReceipt.ApproveInvoiceUT.example
      .withOutExample("Invoice approved", InvoiceReceipt.ApproveInvoiceUT.Out())
      .withOutExample(
        "Invoice NOT approved",
        InvoiceReceipt.ApproveInvoiceUT.Out(false)
      )

  private lazy val ReviewInvoiceUT =
    ReviewInvoice.ReviewInvoiceUT.example
      .withOutExample("Invoice clarified", ReviewInvoice.ReviewInvoiceUT.Out())
      .withOutExample(
        "Invoice NOT clarified",
        ReviewInvoice.ReviewInvoiceUT.Out(false)
      )

  private lazy val budget = InvoiceAssignApproverDMN.In()
  private lazy val `day-to-day expense` =
    InvoiceAssignApproverDMN.In(125, InvoiceCategory.Misc)
  private lazy val exceptional =
    InvoiceAssignApproverDMN.In(12345, InvoiceCategory.Misc)

  private lazy val InvoiceAssignApproverDMN1 =
    InvoiceReceipt.InvoiceAssignApproverDMN.example
      .withInExample(budget)
      .withInExample(`day-to-day expense`)
      .withInExample(exceptional)

  private lazy val InvoiceAssignApproverDMN2 =
    InvoiceReceipt.InvoiceAssignApproverDMN.example
      .withOutExample("budget", CollectEntries(ApproverGroup.management))
      .withOutExample(
        "day-to-day expense",
        CollectEntries(ApproverGroup.accounting, ApproverGroup.sales)
      )

  private lazy val InvoiceAssignApproverDMN3 =
    InvoiceReceipt.InvoiceAssignApproverDMN.example
      .withExample(
        "budget",
        InvoiceReceipt.InvoiceAssignApproverDMN.example
          .withIn(InvoiceReceipt.InvoiceAssignApproverDMN.In())
          .withOut(Seq(ApproverGroup.management))
      )
      .withExample(
        "day-to-day expense",
        InvoiceReceipt.InvoiceAssignApproverDMN.example
          .withIn(
            InvoiceReceipt.InvoiceAssignApproverDMN
              .In(125, InvoiceCategory.Misc)
          )
          .withOut(
            Seq(ApproverGroup.accounting, ApproverGroup.sales)
          )
      )
