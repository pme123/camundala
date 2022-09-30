package camundala
package examples.invoice.api

import api.*
import bpmn.*

import examples.invoice.domain.*
import sttp.tapir.json.circe.*


object InvoiceApi extends BpmnDsl:

  val InvoiceReceiptPIdent = "InvoiceReceiptP"

  lazy val `Invoice Receipt` =
    process(
      id = InvoiceReceiptPIdent,
      descr = cawemoDescr("This starts the Invoice Receipt Process.", "e289c19a-8a57-4467-8583-de72a5e57488" ),
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
  ).withDescr(cawemoDescr("Decision Table on who must approve the Invoice.", "155ba236-d5d1-42f7-8b56-3e90e0bb98d4"))

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

  lazy val ArchiveInvoiceST = serviceTask(
    id = "ArchiveInvoiceST",
    descr = "Archive the Invoice."
  )

  lazy val `Review Invoice clarified`: CallActivity[InvoiceReceipt, InvoiceReviewed] =
    callActivity(
      id = "ReviewInvoiceCA",
      `Review Invoice`.id,
      descr = "Calls the Review Invoice Process and clarifies the Invoice.",
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )

  lazy val `Review Invoice not clarified`: CallActivity[InvoiceReceipt, InvoiceReviewed] =
      callActivity(
        id = "ReviewInvoiceCA",
        `Review Invoice`.id,
        descr = "Calls the Review Invoice Process and does not clarify the Invoice.",
        in = InvoiceReceipt(),
        out = InvoiceReviewed(false)
      )

  lazy val `Review Invoice`: Process[InvoiceReceipt, InvoiceReviewed] =
    val processId = "ReviewInvoiceP"
    process(
      id = processId,
      descr = cawemoDescr("This starts the Review Invoice Process.", "cc9f978a-e98a-4b01-991d-36d682574cda"),
      in = InvoiceReceipt(),
      out = InvoiceReviewed()
    )
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

  val InvoiceNotprocessedIdent = "InvoiceNotProcessedEE"
  lazy val InvoiceNotProcessedEE = endEvent(
    InvoiceNotprocessedIdent,
  )
  val InvoiceProcessedIdent = "InvoiceProcessedEE"
  lazy val InvoiceProcessedEE = endEvent(
    InvoiceProcessedIdent,
  )
