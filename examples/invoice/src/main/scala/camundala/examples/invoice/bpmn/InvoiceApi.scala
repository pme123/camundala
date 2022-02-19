package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import domain.*
import sttp.tapir.json.circe.*
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

  @description("There are three possible Categories")
  enum InvoiceCategory derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
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

  @description("These Groups can approve the invoice.")
  enum ApproverGroup derives JsonTaggedAdt.PureEncoder, JsonTaggedAdt.PureDecoder:
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
/*
  implicit lazy val PrepareBankTransferSchema: Schema[PrepareBankTransfer] = Schema.derived
  implicit lazy val PrepareBankTransferEncoder: Encoder[PrepareBankTransfer] = deriveEncoder
  implicit lazy val PrepareBankTransferDecoder: Decoder[PrepareBankTransfer] = deriveDecoder

  implicit lazy val AssignedReviewerSchema: Schema[AssignedReviewer] = Schema.derived
  implicit lazy val AssignedReviewerEncoder: Encoder[AssignedReviewer] = deriveEncoder
  implicit lazy val AssignedReviewerDecoder: Decoder[AssignedReviewer] = deriveDecoder

  implicit lazy val InvoiceReviewedSchema: Schema[InvoiceReviewed] = Schema.derived
  implicit lazy val InvoiceReviewedEncoder: Encoder[InvoiceReviewed] = deriveEncoder
  implicit lazy val InvoiceReviewedDecoder: Decoder[InvoiceReviewed] = deriveDecoder

  implicit lazy val InvoiceReceiptSchema: Schema[InvoiceReceipt] = Schema.derived
  implicit lazy val InvoiceReceiptEncoder: Encoder[InvoiceReceipt] = deriveEncoder
  implicit lazy val InvoiceReceiptDecoder: Decoder[InvoiceReceipt] = deriveDecoder

  implicit lazy val InvoiceCategorySchema: Schema[InvoiceCategory] = Schema.derived
  implicit lazy val InvoiceCategoryEncoder: Encoder[InvoiceCategory] = deriveEncoder
  implicit lazy val InvoiceCategoryDecoder: Decoder[InvoiceCategory] = deriveDecoder

  implicit lazy val InvoiceReceiptCheckSchema: Schema[InvoiceReceiptCheck] = Schema.derived
  implicit lazy val InvoiceReceiptCheckEncoder: Encoder[InvoiceReceiptCheck] = deriveEncoder
  implicit lazy val InvoiceReceiptCheckDecoder: Decoder[InvoiceReceiptCheck] = deriveDecoder

  implicit lazy val ApproveInvoiceSchema: Schema[ApproveInvoice] = Schema.derived
  implicit lazy val ApproveInvoiceEncoder: Encoder[ApproveInvoice] = deriveEncoder
  implicit lazy val ApproveInvoiceDecoder: Decoder[ApproveInvoice] = deriveDecoder

  implicit lazy val SelectApproverGroupSchema: Schema[SelectApproverGroup] = Schema.derived
  implicit lazy val SelectApproverGroupEncoder: Encoder[SelectApproverGroup] = deriveEncoder
  implicit lazy val SelectApproverGroupDecoder: Decoder[SelectApproverGroup] = deriveDecoder

  implicit lazy val ApproverGroupSchema: Schema[ApproverGroup] = Schema.derived

  implicit lazy val AssignApproverGroupsSchema: Schema[AssignApproverGroups] = Schema.derived
  implicit lazy val AssignApproverGroupsEncoder: Encoder[AssignApproverGroups] = deriveEncoder
  implicit lazy val AssignApproverGroupsDecoder: Decoder[AssignApproverGroups] = deriveDecoder
*/

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val InvoiceReceiptP =
    process(
      id = InvoiceReceiptPIdent,
      descr = cawemoDescr("This starts the Invoice Receipt Process.", "e289c19a-8a57-4467-8583-de72a5e57488" ),
      in = InvoiceReceipt(),
      out = InvoiceReceiptCheck() // just for testing
    )

  lazy val invoiceAssignApproverDMN
      : DecisionDmn[SelectApproverGroup, AssignApproverGroups] = collectEntries(
    decisionDefinitionKey = "invoice-assign-approver",
    in = SelectApproverGroup(),
    out = AssignApproverGroups(),
  ).withDescr(cawemoDescr("Decision Table on who must approve the Invoice.", "155ba236-d5d1-42f7-8b56-3e90e0bb98d4"))

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
      descr = cawemoDescr("This starts the Review Invoice Process.", "cc9f978a-e98a-4b01-991d-36d682574cda"),
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

