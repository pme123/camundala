package camundala.examples.invoice

import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.domain.*

object bpmn extends BpmnDsl:

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val `Invoice Receipt` =
    process(
      id = InvoiceReceiptPIdent,
      descr = "This starts the Invoice Receipt Process.",
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
    descr = "Decision Table on who must approve the Invoice."
  )

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

  lazy val `Review Invoice` =
    val processId = "ReviewInvoiceP"
    process(
      id = processId,
      descr = "This starts the Review Invoice Process.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  end `Review Invoice`
  lazy val `Review Invoice not clarified` =
    `Review Invoice`
      .withOut(InvoiceReviewed(false))

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

end bpmn
