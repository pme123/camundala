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

  object InvoiceReceipt:
    given ApiSchema[InvoiceReceipt] = deriveApiSchema
    given InOutCodec[InvoiceReceipt] = deriveCodec
  end InvoiceReceipt

  @description("There are three possible Categories")
  enum InvoiceCategory :
    case `Travel Expenses`, Misc, `Software License Costs`
  object InvoiceCategory:
    given ApiSchema[InvoiceCategory] = deriveEnumApiSchema
    given InOutCodec[InvoiceCategory] = deriveEnumInOutCodec
  end InvoiceCategory

  case class SelectApproverGroup(
                                  amount: Double = 30.0,
                                  invoiceCategory: InvoiceCategory =
                                  InvoiceCategory.`Software License Costs`
                                )
  object SelectApproverGroup:
    given ApiSchema[SelectApproverGroup] = deriveApiSchema
    given InOutCodec[SelectApproverGroup] = deriveCodec
  end SelectApproverGroup

  @description("These Groups can approve the invoice.")
  enum ApproverGroup :
    case accounting, sales, management
  object ApproverGroup:
    given ApiSchema[ApproverGroup] = deriveEnumApiSchema
    given InOutCodec[ApproverGroup] = deriveEnumInOutCodec
  end ApproverGroup

  @description("""Every Invoice has to be accepted by the Boss.""")
  case class ApproveInvoice(
                             @description("If true, the Boss accepted the Invoice")
                             approved: Boolean = true
                           )
  object ApproveInvoice:
    given ApiSchema[ApproveInvoice] = deriveApiSchema
    given InOutCodec[ApproveInvoice] = deriveCodec
  end ApproveInvoice

  @description(
    """Prepares the bank transfer for the invoice. Only readOnly fields from the Process."""
  )
  case class PrepareBankTransfer(
                                )
  object PrepareBankTransfer:
    given ApiSchema[PrepareBankTransfer] = deriveApiSchema
    given InOutCodec[PrepareBankTransfer] = deriveCodec
  end PrepareBankTransfer

  case class AssignedReviewer(reviewer: String = "John")
  object AssignedReviewer:
    given ApiSchema[AssignedReviewer] = deriveApiSchema
    given InOutCodec[AssignedReviewer] = deriveCodec
  end AssignedReviewer

  case class InvoiceReviewed(
                              @description("Flag that is set by the Reviewer")
                              clarified: Boolean = true
                            )

  object InvoiceReviewed:
    given ApiSchema[InvoiceReviewed] = deriveApiSchema
    given InOutCodec[InvoiceReviewed] = deriveCodec
  end InvoiceReviewed

  case class InvoiceReceiptCheck(
                                  @description("If true, the Boss accepted the Invoice")
                                  approved: Option[Boolean] = Some(true),
                                  @description("Flag that is set by the Reviewer (only set if there was a review).")
                                  clarified: Option[Boolean] = None,
                                  @description("The groups selected from the DMN")
                                  approverGroups: Seq[String] = Seq("accounting", "sales")
                                )

  object InvoiceReceiptCheck:
    given ApiSchema[InvoiceReceiptCheck] = deriveApiSchema
    given InOutCodec[InvoiceReceiptCheck] = deriveCodec
  end InvoiceReceiptCheck


end domain

