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
    given CirceCodec[InvoiceReceipt] = deriveCodec
  end InvoiceReceipt

  @description("There are three possible Categories")
  enum InvoiceCategory derives ConfiguredEnumCodec :
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
    given CirceCodec[SelectApproverGroup] = deriveCodec
  end SelectApproverGroup

  @description("These Groups can approve the invoice.")
  enum ApproverGroup derives ConfiguredEnumCodec :
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
    given CirceCodec[ApproveInvoice] = deriveCodec
  end ApproveInvoice

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
                                )
  object PrepareBankTransfer:
    given Schema[PrepareBankTransfer] = Schema.derived
    given CirceCodec[PrepareBankTransfer] = deriveCodec
  end PrepareBankTransfer

  case class AssignedReviewer(reviewer: String = "John")
  object AssignedReviewer:
    given Schema[AssignedReviewer] = Schema.derived
    given CirceCodec[AssignedReviewer] = deriveCodec
  end AssignedReviewer

  case class InvoiceReviewed(
                              @description("Flag that is set by the Reviewer")
                              clarified: Boolean = true
                            )
  object InvoiceReviewed:
    given Schema[InvoiceReviewed] = Schema.derived
    given CirceCodec[InvoiceReviewed] = deriveCodec
  end InvoiceReviewed

  case class InvoiceReceiptCheck(
                                  @description("If true, the Boss accepted the Invoice")
                                  approved: Boolean = true,
                                  @description("Flag that is set by the Reviewer (only set if there was a review).")
                                  clarified: Option[Boolean] = None
                                )
  object InvoiceReceiptCheck:
    given Schema[InvoiceReceiptCheck] = Schema.derived
    given CirceCodec[InvoiceReceiptCheck] = deriveCodec
  end InvoiceReceiptCheck

end domain

