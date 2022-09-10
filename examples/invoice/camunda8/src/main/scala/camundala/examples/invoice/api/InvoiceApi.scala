package camundala
package examples.invoice.api

import api.*
import bpmn.*

import sttp.tapir.json.circe.*

object InvoiceApi extends BpmnDsl:

  @description("Received Invoice that need approval.")
  case class InvoiceReceipt(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345",
      invoiceDocument: FileRefInOut = FileRefInOut(
        "invoice.pdf",
        "processes/invoice.pdf",
        Some("application/pdf")
      )
  )

  @description("There are three possible Categories")
  enum InvoiceCategory derives Adt.PureEncoder, Adt.PureDecoder:
    case `Travel Expenses`, Misc, `Software License Costs`

  case class SelectApproverGroup(
      amount: Double = 30.0,
      invoiceCategory: InvoiceCategory =
        InvoiceCategory.`Software License Costs`
  )


  @description("These Groups can approve the invoice.")
  enum ApproverGroup derives Adt.PureEncoder, Adt.PureDecoder:
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
      approved: Option[Boolean] = Some(true),
      @description("Flag that is set by the Reviewer (only set if there was a review).")
      clarified: Option[Boolean] = None,
      @description("The groups selected from the DMN")
      approverGroups: Seq[String] = Seq("accounting","sales")
  )

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val `Invoice Receipt` =
    process(
      id = InvoiceReceiptPIdent,
      descr = cawemoDescr("This starts the Invoice Receipt Process.", "e289c19a-8a57-4467-8583-de72a5e57488" ),
      in = InvoiceReceipt(),
      out = InvoiceReceiptCheck() // just for testing
    )

  lazy val `Invoice Receipt with Review` =
    `Invoice Receipt`
      .withOut(InvoiceReceiptCheck(clarified = Some(true)))

  lazy val `Invoice Receipt with Review failed` =
    `Invoice Receipt`
      .withOut(
        InvoiceReceiptCheck(approved = Some(false), clarified = Some(false))
      )
  lazy val BadValidationP =
    `Invoice Receipt`
      .withIn(InvoiceReceipt(null))

  lazy val InvoiceAssignApproverDMN = collectEntries(
    decisionDefinitionKey = "invoice-assign-approver",
    in = SelectApproverGroup(),
    out = Seq(ApproverGroup.management),
  ).withDescr(cawemoDescr("Decision Table on who must approve the Invoice.", "155ba236-d5d1-42f7-8b56-3e90e0bb98d4"))

  lazy val InvoiceAssignApproverDMN2 =
    InvoiceAssignApproverDMN
      .withIn(SelectApproverGroup(1050, InvoiceCategory.`Travel Expenses`))
      .withOut(
        CollectEntries(Seq(ApproverGroup.accounting, ApproverGroup.sales))
      )

  lazy val AssignApproverGroupBRT = // for unit testing you need the BusinessRuleTask
    InvoiceAssignApproverDMN2
      .withId("AssignApproverGroupBRT")

  lazy val ApproveInvoiceUT =
    userTask(
      id = "ApproveInvoiceUT",
      descr = "Approve the invoice (or not).",
      in = InvoiceReceipt(),
      out = ApproveInvoice()
    )

  lazy val PrepareBankTransferUT = userTask(
    id = "PrepareBankTransferUT",
    descr = "Prepare the bank transfer in the Financial Accounting System.",
    in = InvoiceReceipt(),
    out = PrepareBankTransfer()
  )

  lazy val ArchiveInvoiceST = serviceTask(
    id = "ArchiveInvoiceST",
    descr = "Archive the Invoice."
  )

  lazy val `Review Invoice clarified`: CallActivity[InvoiceReceipt, InvoiceReviewed] =
    callActivity(
      id = "ReviewInvoiceCA",
      `Review Invoice`.id,
      descr = "Calls the Review Invoice Process and clarifies the Invoice.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )

  lazy val `Review Invoice not clarified`: CallActivity[InvoiceReceipt, InvoiceReviewed] =
      callActivity(
        id = "ReviewInvoiceCA",
        `Review Invoice`.id,
        descr = "Calls the Review Invoice Process and does not clarify the Invoice.",
        in = InvoiceReceipt(),
        out = InvoiceReviewed(false)
      )

  lazy val `Review Invoice`: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoiceP"
    process(
      id = processId,
      descr = cawemoDescr("This starts the Review Invoice Process.", "cc9f978a-e98a-4b01-991d-36d682574cda"),
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  lazy val AssignReviewerUT = userTask(
    id = "AssignReviewerUT",
    descr = "Select the Reviewer.",
    in = InvoiceReceipt(),
    out = AssignedReviewer()
  )
  lazy val ReviewInvoiceUT = userTask(
    id = "ReviewInvoiceUT",
    descr = "Review Invoice and approve.",
    in = InvoiceReceipt(),
    out = InvoiceReviewed()
  )

  val InvoiceNotprocessedIdent = "InvoiceNotProcessedEE"
  lazy val InvoiceNotProcessedEE = endEvent(
    InvoiceNotprocessedIdent,
  )
  val InvoiceProcessedIdent = "InvoiceProcessedEE"
  lazy val InvoiceProcessedEE = endEvent(
    InvoiceProcessedIdent,
  )

  given Schema[InvoiceReceipt] = Schema.derived

  given Encoder[InvoiceReceipt] = deriveEncoder

  given Decoder[InvoiceReceipt] = deriveDecoder

  given Schema[InvoiceCategory] = Schema.derived

  given Schema[SelectApproverGroup] = Schema.derived

  given Encoder[SelectApproverGroup] = deriveEncoder

  given Decoder[SelectApproverGroup] = deriveDecoder

  given Schema[ApproverGroup] = Schema.derived

  given Schema[ApproveInvoice] = Schema.derived

  given Encoder[ApproveInvoice] = deriveEncoder

  given Decoder[ApproveInvoice] = deriveDecoder

  given Schema[PrepareBankTransfer] = Schema.derived

  given Encoder[PrepareBankTransfer] = deriveEncoder

  given Decoder[PrepareBankTransfer] = deriveDecoder

  given Schema[AssignedReviewer] = Schema.derived

  given Encoder[AssignedReviewer] = deriveEncoder

  given Decoder[AssignedReviewer] = deriveDecoder

  given Schema[InvoiceReviewed] = Schema.derived

  given Encoder[InvoiceReviewed] = deriveEncoder

  given Decoder[InvoiceReviewed] = deriveDecoder

  given Schema[InvoiceReceiptCheck] = Schema.derived

  given Encoder[InvoiceReceiptCheck] = deriveEncoder

  given Decoder[InvoiceReceiptCheck] = deriveDecoder
