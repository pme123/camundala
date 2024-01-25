package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.domain.*

object ReviewInvoice extends BpmnDsl:
  final val processName = "example-invoice-c7-review"

  type InConfig = NoInConfig

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345"
  )
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
      @description("Flag that is set by the Reviewer")
      clarified: Boolean = true
  )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  lazy val example =
    process(
      id = processName,
      descr = "This starts the Review Invoice Process.",
      in = In(),
      out = Out(),
    )

  object AssignReviewerUT:

    // same Input
    type In = InvoiceReceipt.PrepareBankTransferUT.In

    case class Out(
        reviewer: String = "John"
    )
    object Out:
      given ApiSchema[Out] = deriveApiSchema
      given InOutCodec[Out] = deriveCodec
    end Out

    lazy val example: UserTask[In, Out] =
      userTask(
        id = "AssignReviewerUT",
        descr = "Select the Reviewer.",
        in = InvoiceReceipt.PrepareBankTransferUT.In(),
        out = Out()
      )
  end AssignReviewerUT

  object ReviewInvoiceUT:

    // same Input
    type In = InvoiceReceipt.PrepareBankTransferUT.In

    case class Out(
        @description("Flag that is set by the Reviewer")
        clarified: Boolean = true
    )
    object Out:
      given ApiSchema[Out] = deriveApiSchema
      given InOutCodec[Out] = deriveCodec
    end Out

    lazy val example: UserTask[In, Out] =
      userTask(
        id = "ReviewInvoiceUT",
        descr = "Review Invoice and approve.",
        in = InvoiceReceipt.PrepareBankTransferUT.In(),
        out = Out()
      )
  end ReviewInvoiceUT

end ReviewInvoice
