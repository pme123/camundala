package camundala.examples.invoice

import domain.*
import camundala.domain.*
import camundala.bpmn.*

object bpmn extends BpmnDsl:

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val `Invoice Receipt` =
    process(
      id = InvoiceReceiptPIdent,
      descr = cawemoDescr(
        "This starts the Invoice Receipt Process.",
        "e289c19a-8a57-4467-8583-de72a5e57488"
      ),
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
    descr = cawemoDescr(
      "Decision Table on who must approve the Invoice.",
      "155ba236-d5d1-42f7-8b56-3e90e0bb98d4"
    )
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

  lazy val `Review Invoice`: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoiceP"
    process(
      id = processId,
      descr = cawemoDescr(
        "This starts the Review Invoice Process.",
        "cc9f978a-e98a-4b01-991d-36d682574cda"
      ),
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
  lazy val `Review Invoice not clarified`: Process[InvoiceReceipt, InvoiceReviewed] =
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
