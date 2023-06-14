package camundala.examples.invoice

import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.InvoiceReceipt.InvoiceCategory

object ReviewInvoice extends BpmnDsl:
  val processName = "example-invoice-c7-review"

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345",
      @description("You can let the Archive Service fail for testing.")
      shouldFail: Option[Boolean] = None,
      @description(outputMockDescr(ReviewInvoice.Out()))
      outputMock: Option[Out] = None
  )
  object In:
    given Schema[In] = Schema.derived
    given CirceCodec[In] = deriveCodec
  end In

  case class Out(
      @description("Flag that is set by the Reviewer")
      clarified: Boolean = true
  )
  object Out:
    given Schema[Out] = Schema.derived
    given CirceCodec[Out] = deriveCodec
  end Out

  lazy val example: Process[In, Out] =
    process(
      id = processName,
      descr = //cawemoDescr(
        "This starts the Review Invoice Process.",
        //"cc9f978a-e98a-4b01-991d-36d682574cda"),
      in = In(),
      out = Out()
    )

  object AssignReviewerUT:

    // same Input
    type In = InvoiceReceipt.PrepareBankTransferUT.In

    case class Out(
                    reviewer: String = "John"
                  )
    object Out:
      given Schema[Out] = Schema.derived
      given CirceCodec[Out] = deriveCodec
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
      given Schema[Out] = Schema.derived
      given CirceCodec[Out] = deriveCodec
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
