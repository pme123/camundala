package camundala.examples.invoice

import camundala.domain.*

object domain :
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
  enum InvoiceCategory derives Adt.PureEncoder, Adt.PureDecoder :
    case `Travel Expenses`, Misc, `Software License Costs`

  case class SelectApproverGroup(
                                  amount: Double = 30.0,
                                  invoiceCategory: InvoiceCategory =
                                  InvoiceCategory.`Software License Costs`
                                )


  @description("These Groups can approve the invoice.")
  enum ApproverGroup derives Adt.PureEncoder, Adt.PureDecoder :
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
                                  approverGroups: Seq[String] = Seq("accounting", "sales")
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

end domain

