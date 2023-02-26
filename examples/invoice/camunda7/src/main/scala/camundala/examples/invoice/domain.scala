package camundala.examples.invoice

import camundala.domain.*

object domain :

  case class InvoiceReceipt(
                           creditor: String = "Great Pizza for Everyone Inc.",
                           amount: Double = 300.0,
                           invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
                           invoiceNumber: String = "I-12345",
                           // removed due to problems with sttp client
                           /*   invoiceDocument: FileInOut = FileInOut(
                                "invoice.pdf",
                                read.bytes(
                                  os.resource / "invoice.pdf"
                                ),
                                Some("application/pdf")
                              )*/
                           @description("You can let the Archive Service fail for testing.")
                           shouldFail: Option[Boolean] = None
                         )
  object InvoiceReceipt:
    given Schema[InvoiceReceipt] = Schema.derived
    given Encoder[InvoiceReceipt] = deriveEncoder
    given Decoder[InvoiceReceipt] = deriveDecoder
  end InvoiceReceipt

  @description("There are three possible Categories")
  enum InvoiceCategory derives Adt.PureEncoder, Adt.PureDecoder :
    case `Travel Expenses`, Misc, `Software License Costs`
  object InvoiceCategory:
    given Schema[InvoiceCategory] = Schema.derived

  case class SelectApproverGroup(
                                  amount: Double = 30.0,
                                  invoiceCategory: InvoiceCategory =
                                  InvoiceCategory.`Software License Costs`
                                )
  object SelectApproverGroup:
    given Schema[SelectApproverGroup] = Schema.derived
    given Encoder[SelectApproverGroup] = deriveEncoder
    given Decoder[SelectApproverGroup] = deriveDecoder
  end SelectApproverGroup

  @description("These Groups can approve the invoice.")
  enum ApproverGroup derives Adt.PureEncoder, Adt.PureDecoder :
    case accounting, sales, management
  object ApproverGroup:
    given Schema[ApproverGroup] = Schema.derived

  @description("""Every Invoice has to be accepted by the Boss.""")
  case class ApproveInvoice(
                             @description("If true, the Boss accepted the Invoice")
                             approved: Boolean = true
                           )
  object ApproveInvoice:
    given Schema[ApproveInvoice] = Schema.derived
    given Encoder[ApproveInvoice] = deriveEncoder
    given Decoder[ApproveInvoice] = deriveDecoder
  end ApproveInvoice

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
                                )
  object PrepareBankTransfer:
    given Schema[PrepareBankTransfer] = Schema.derived
    given Encoder[PrepareBankTransfer] = deriveEncoder
    given Decoder[PrepareBankTransfer] = deriveDecoder
  end PrepareBankTransfer

  case class AssignedReviewer(reviewer: String = "John")
  object AssignedReviewer:
    given Schema[AssignedReviewer] = Schema.derived
    given Encoder[AssignedReviewer] = deriveEncoder
    given Decoder[AssignedReviewer] = deriveDecoder
  end AssignedReviewer

  case class InvoiceReviewed(
                              @description("Flag that is set by the Reviewer")
                              clarified: Boolean = true
                            )
  object InvoiceReviewed:
    given Schema[InvoiceReviewed] = Schema.derived
    given Encoder[InvoiceReviewed] = deriveEncoder
    given Decoder[InvoiceReviewed] = deriveDecoder
  end InvoiceReviewed

  case class InvoiceReceiptCheck(
                                  @description("If true, the Boss accepted the Invoice")
                                  approved: Boolean = true,
                                  @description("Flag that is set by the Reviewer (only set if there was a review).")
                                  clarified: Option[Boolean] = None
                                )
  object InvoiceReceiptCheck:
    given Schema[InvoiceReceiptCheck] = Schema.derived
    given Encoder[InvoiceReceiptCheck] = deriveEncoder
    given Decoder[InvoiceReceiptCheck] = deriveDecoder
  end InvoiceReceiptCheck

end domain

