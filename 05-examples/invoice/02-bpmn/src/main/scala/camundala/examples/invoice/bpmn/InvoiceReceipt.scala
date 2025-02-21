package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.bpmn.ReviewInvoice.Out

object InvoiceReceipt extends BpmnProcessDsl:

  val processName = "example-invoice-c7"
  val descr = "This starts the Invoice Receipt Process."

  case class In(
      creditor: String = "Great Pizza for Everyone Inc.",
      amount: Double = 300.0,
      invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`,
      invoiceNumber: String = "I-12345",
      inConfig: Option[InConfig] = None
  ) extends WithConfig[InConfig]:

    lazy val defaultConfig: InConfig = InConfig()
  end In

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  type InitIn = NoInput

  case class Out(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true,
      @description(
        "Flag that is set by the Reviewer (only set if there was a review)."
      )
      clarified: Option[Boolean] = None,
      @description(
        "Flag that is set by the Archive Service."
      )
      archived: Option[Boolean] = Some(false)
  )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  case class InConfig(
      @description("You can let the Archive Service fail for testing.")
      shouldFail: Option[Boolean] = None,
      @description(serviceOrProcessMockDescr(ReviewInvoice.Out()))
      invoiceReviewedMock: Option[ReviewInvoice.Out] = None
  )
  object InConfig:
    lazy val example: InConfig = InConfig(
      shouldFail = Some(false),
      invoiceReviewedMock = Some(ReviewInvoice.Out())
    )
    given ApiSchema[InConfig] = deriveApiSchema
    given InOutCodec[InConfig] = deriveCodec
  end InConfig

  lazy val example =
    process(
      in = In(),
      out = Out() // just for testing
    )

  object InvoiceAssignApproverDMN extends BpmnDecisionDsl:
    val decisionId = "example-invoice-c7-assignApprover"
    val descr = "Decision Table on who must approve the Invoice."

    case class In(
        amount: Double = 30.0,
        invoiceCategory: InvoiceCategory =
          InvoiceCategory.`Software License Costs`
    )
    object In:
      given ApiSchema[In] = deriveApiSchema
      given InOutCodec[In] = deriveCodec
    end In

    type Out = Seq[ApproverGroup]

    lazy val example: DecisionDmn[In, CollectEntries[ApproverGroup]] =
      collectEntries(
        in = In(),
        out = Seq(ApproverGroup.management),
      )
  end InvoiceAssignApproverDMN

  object InvoiceAssignApproverDmnUnit extends BpmnDecisionDsl:
    val decisionId = "example-invoice-c7-assignApprover"
    val descr = "Decision Table just for unit testing."

    case class In(
                   invoiceClassification: InvoiceClassification = InvoiceClassification.`day-to-day expense`
                 )
    object In:
      given ApiSchema[In] = deriveApiSchema
      given InOutCodec[In] = deriveCodec
    end In

    @description("There are three possible Categories")
    enum InvoiceClassification:
      case `day-to-day expense`, budget, exceptional

    object InvoiceClassification:
      given Schema[InvoiceClassification] = deriveEnumApiSchema
      given InOutCodec[InvoiceClassification] = deriveEnumInOutCodec

    type Out = Seq[ApproverGroup]

    lazy val example: DecisionDmn[In, CollectEntries[ApproverGroup]] =
      collectEntries(
        in = In(),
        out = Seq(ApproverGroup.management),
      )

  object ApproveInvoiceUT:
    type In = InvoiceReceipt.PrepareBankTransferUT.In

    @description("""Every Invoice has to be accepted by the Boss.""")
    case class Out(
        @description("If true, the Boss accepted the Invoice")
        approved: Boolean = true
    )
    object Out:
      given ApiSchema[Out] = deriveApiSchema
      given InOutCodec[Out] = deriveCodec
    end Out

    lazy val example: UserTask[In, Out] =
      userTask(
        id = "ApproveInvoiceUT",
        descr = "Approve the invoice (or not).",
        in = InvoiceReceipt.PrepareBankTransferUT.In(),
        out = Out()
      )
  end ApproveInvoiceUT

  object PrepareBankTransferUT:
    @description(
      "Same Input as _InvoiceReceipt_, only without mocking - no mocking in UserTasks"
    )
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

    type Out = NoOutput

    lazy val example: UserTask[In, Out] =
      userTask(
        id = "PrepareBankTransferUT",
        descr = "Prepare the bank transfer in the Financial Accounting System.",
        in = In(),
        out = NoOutput()
      )
  end PrepareBankTransferUT

  enum ApproverGroup:
    case accounting, sales, management
  object ApproverGroup:
    given ApiSchema[ApproverGroup] = deriveEnumApiSchema
    given InOutCodec[ApproverGroup] = deriveEnumInOutCodec

  end ApproverGroup

end InvoiceReceipt
