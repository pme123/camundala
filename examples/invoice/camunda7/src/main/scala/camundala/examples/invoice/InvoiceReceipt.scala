package camundala.examples.invoice

import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.ReviewInvoice.Out

import scala.collection.immutable.Seq

object InvoiceReceipt extends BpmnDsl:

  val processName = "example-invoice-c7"

  case class  In(
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
      shouldFail: Option[Boolean] = None,
      @description(outputMockDescr(ReviewInvoice.Out()))
      invoiceReviewedMock: Option[ReviewInvoice.Out] = None
  )
  object In:
    given Schema[In] = Schema.derived
    given CirceCodec[In] = deriveCodec
  end In

  case class Out(
      @description("If true, the Boss accepted the Invoice")
      approved: Boolean = true,
      @description(
        "Flag that is set by the Reviewer (only set if there was a review)."
      )
      clarified: Option[Boolean] = None
  )
  object Out:
    given Schema[Out] = Schema.derived
    given CirceCodec[Out] = deriveCodec
  end Out

  lazy val example: Process[In, Out] =
    process(
      id = processName,
      descr = //cawemoDescr(
        "This starts the Invoice Receipt Process.",
      // "e289c19a-8a57-4467-8583-de72a5e57488"      ),
      in = In(),
      out = Out() // just for testing
    )

  object InvoiceAssignApproverDMN:
    case class In(
        amount: Double = 30.0,
        invoiceCategory: InvoiceCategory =
          InvoiceCategory.`Software License Costs`
    )
    object In:
      given Schema[In] = Schema.derived
      given CirceCodec[In] = deriveCodec
    end In

    type Out = Seq[ApproverGroup]

    lazy val example: DecisionDmn[In, CollectEntries[ApproverGroup]] =
      collectEntries(
        decisionDefinitionKey = "example-invoice-c7-assignApprover",
        in = In(),
        out = Seq(ApproverGroup.management),
        descr = //cawemoDescr(
          "Decision Table on who must approve the Invoice.",
        // "155ba236-d5d1-42f7-8b56-3e90e0bb98d4" )
      )
  end InvoiceAssignApproverDMN

  object ApproveInvoiceUT:
    type In = InvoiceReceipt.PrepareBankTransferUT.In

    @description("""Every Invoice has to be accepted by the Boss.""")
    case class Out(
        @description("If true, the Boss accepted the Invoice")
        approved: Boolean = true
    )
    object Out:
      given Schema[Out] = Schema.derived
      given CirceCodec[Out] = deriveCodec
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
        invoiceNumber: String = "I-12345",
    )
    object In:
      given Schema[In] = Schema.derived
      given CirceCodec[In] = deriveCodec
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

  enum ApproverGroup derives ConfiguredEnumCodec:
    case accounting, sales, management
  object ApproverGroup:
    given Schema[ApproverGroup] = Schema.derived
  end ApproverGroup

end InvoiceReceipt

// example for service API description
object ArchiveInvoice extends BpmnDsl:

  final val serviceName = "ArchiveInvoiceService"
  type ServiceIn = NoInput
  type ServiceOut = Seq[String]
  lazy val serviceMock: ServiceOut = Seq("someIdJustForDemo")

  case class  In(
                  shouldFail: Boolean = true,
                )
  object In:
    given Schema[In] = Schema.derived
    given CirceCodec[In] = deriveCodec
  end In

  case class Out(
                  archived: Option[Boolean] = Some(true),
                )
  object Out:
    given Schema[Out] = Schema.derived
    given CirceCodec[Out] = deriveCodec
  end Out

 // given CirceCodec[Seq[String]] = deriveCodec

  lazy val example: ServiceProcess[In, Out, ServiceOut] =
    serviceProcess(
      serviceName,
      descr = "Archives the Receipt.",
      in = In(),
      out = Out() ,
      defaultServiceMock = serviceMock
    )

end ArchiveInvoice
