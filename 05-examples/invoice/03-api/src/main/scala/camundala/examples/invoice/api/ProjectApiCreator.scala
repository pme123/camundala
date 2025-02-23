// put your api in a package of your project
package camundala.examples.invoice
package api

import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.bpmn.InvoiceReceipt.*
import camundala.examples.invoice.bpmn.InvoiceReceipt.InvoiceAssignApproverDMN.*

// define an object that extends from a common Api Creator
// exampleInvoiceApi/run
object ProjectApiCreator extends DefaultApiCreator:

  // technical name of the project
  val projectName                        = "example-invoice"
  // readable name of the project
  protected val title                    = "Invoice Example Process API"
  // version of your project
  protected val version                  = "1.0"
  lazy val companyProjectVersion: String = "0.1.0"

  lazy val companyDescr: String = ""
  lazy val projectDescr: String = ""

  document(
    api(`Invoice Receipt`)(
      InvoiceAssignApproverDMN1,
      ApproveInvoiceUT,
      InvoiceReceipt.PrepareBankTransferUT.example,
      api(ArchiveInvoice.example)
    ),
    api(ReviewInvoice.example)(
      ReviewInvoice.AssignReviewerUT.example,
      ReviewInvoiceUT
    ),
    group("Workers", "All my super Workers")(
      StarWarsPeopleDetail.example
        .withOutExample("Success", StarWarsPeopleDetail.Out.Success())
        .withOutExample("Failure", StarWarsPeopleDetail.Out.Failure()),
      api(ArchiveInvoice.example)
    ),
    group("User Tasks", "All the User Interfaces.")(
      api(ApproveInvoiceUT), // api( is optional
      InvoiceReceipt.PrepareBankTransferUT.example,
      ReviewInvoice.AssignReviewerUT.example,
      ReviewInvoiceUT
    ),
    group("DMNs")(
      api(InvoiceAssignApproverDMN2) // api( is optional)
      // InvoiceAssignApproverDMN3 // want be shown as only one DMN with the same id is shown in the API.
    )
  )

  override protected lazy val apiConfig: ApiConfig =
    ApiConfig("demoCompany")
      .withBasePath(os.pwd / "05-examples" / "invoice")
      .withDocBaseUrl(s"https://webstor.ch/camundala/myCompany")
      .withPort(8034)

  private lazy val `Invoice Receipt` =
    InvoiceReceipt.example
      .withInExample(
        "With InConfig",
        InvoiceReceipt.In(inConfig = Some(InConfig.example))
      )
  private lazy val ApproveInvoiceUT  =
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

  private lazy val budget               = InvoiceAssignApproverDMN.In()
  private lazy val `day-to-day expense` =
    InvoiceAssignApproverDMN.In(125, InvoiceCategory.Misc)
  private lazy val exceptional          =
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
end ProjectApiCreator
