package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object InvoiceApi extends BpmnDsl:

  @description("Received Invoice that need approval.")
  case class InvoiceReceipt(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345",
      invoiceDocument: FileInOut = FileInOut(
        "invoice.pdf",
        read.bytes(
          os.resource / "invoice.pdf"
        ),
        Some("application/pdf")
      )
  )

  val invoiceCategoryDescr: String =
    enumDescr[InvoiceCategory]("There are three possible Categories")

  @description(invoiceCategoryDescr)
  enum InvoiceCategory derives JsonTaggedAdt.PureEncoder:
    case `Travel Expenses`
    case Misc
    case `Software License Costs`

  case class SelectApproverGroup(
      amount: Double = 30.0,
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`
  )

  case class AssignApproverGroups(
      approverGroups: Seq[ApproverGroup] = Seq(ApproverGroup.management)
  )

  val approverGroupDescr: String = enumDescr[ApproverGroup](
    "The following Groups can approve the invoice:"
  )
  @description(approverGroupDescr)
  enum ApproverGroup derives JsonTaggedAdt.PureEncoder:
    case accounting, sales, management

  @description("""Every Invoice has to be accepted by the Boss.""")
  case class ApproveInvoice(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true
  )

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
  )

  case class AssignedReviewer(reviewer: String = "John")
  case class InvoiceReviewed(
      @description("Flag that is set by the Reviewer")
      clarified: Boolean = true
  )

  case class InvoiceReceiptCheck(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true,
      @description("Flag that is set by the Reviewer (only set if there was a review).")
      clarified: Option[Boolean] = None
  )

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val InvoiceReceiptP =
    process(
      id = InvoiceReceiptPIdent,
      descr = "This starts the Invoice Receipt Process.",
      in = InvoiceReceipt(),
      out = InvoiceReceiptCheck() // just for testing
    )

  lazy val invoiceAssignApproverDMN
      : DecisionDmn[SelectApproverGroup, AssignApproverGroups] = collectEntries(
    decisionDefinitionKey = "invoice-assign-approver",
    in = SelectApproverGroup(),
    out = AssignApproverGroups()
  )

  lazy val invoiceAssignApproverDMN2
      : DecisionDmn[SelectApproverGroup, AssignApproverGroups] =
    invoiceAssignApproverDMN
      .withIn(SelectApproverGroup(1050, InvoiceCategory.`Travel Expenses`))
      .withOut(
        AssignApproverGroups(Seq(ApproverGroup.accounting, ApproverGroup.sales))
      )

  lazy val approveInvoiceUT =
    userTask(
      id = "ApproveInvoiceUT",
      descr = "Approve the invoice (or not).",
      in = InvoiceReceipt(),
      out = ApproveInvoice()
    )

  lazy val prepareBankTransferUT = userTask(
    id = "PrepareBankTransferUT",
    descr = "Prepare the bank transfer in the Financial Accounting System.",
    in = InvoiceReceipt(),
    out = PrepareBankTransfer()
  )

  lazy val archiveInvoiceST = serviceTask(
    id = "ArchiveInvoiceST",
    descr = "Archive the Invoice."
  )

  lazy val reviewInvoiceCA: CallActivity[InvoiceReceipt, InvoiceReviewed] =
    callActivity(
      id = "ReviewInvoiceCA",
      descr = "Calles the Review Invoice Process.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )

  lazy val ReviewInvoiceP: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoiceP"
    process(
      id = processId,
      descr = "This starts the Review Invoice Process.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  lazy val assignReviewerUT = userTask(
    id = "AssignReviewerUT",
    descr = "Select the Reviewer.",
    in = InvoiceReceipt(),
    out = AssignedReviewer()
  )
  lazy val reviewInvoiceUT = userTask(
    id = "ReviewInvoiceUT",
    descr = "Review Invoice and approve.",
    in = InvoiceReceipt(),
    out = InvoiceReviewed()
  )

  val InvoiceNotprocessedIdent = "InvoiceNotProcessedEE"
  lazy val InvoiceNotProcessedEE = endEvent(
    InvoiceNotprocessedIdent,
    descr = None
  )
  val InvoiceProcessedIdent = "InvoiceProcessedEE"
  lazy val InvoiceProcessedEE = endEvent(
    InvoiceProcessedIdent,
    descr = None
  )

