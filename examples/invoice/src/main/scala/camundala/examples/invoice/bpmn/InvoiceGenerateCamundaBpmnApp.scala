package camundala
package examples.invoice.bpmn

import bpmn.*
import domain.*
import camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object InvoiceGenerateCamundaBpmnApp extends GenerateCamundaBpmn, App:

  val projectPath = pwd / "examples" / "invoice"
  import InvoiceDomain.*
  run(Bpmn(withIdPath / "invoice.v2.bpmn", InvoiceReceiptP),
    Bpmn(withIdPath / "reviewInvoice.bpmn", ReviewInvoiceP))

end InvoiceGenerateCamundaBpmnApp
object InvoiceDomain extends BpmnDsl:

  // invoice.v2.bpmn
  val InvoiceReceiptPIdent ="InvoiceReceiptP"
  lazy val InvoiceReceiptP = process(
    InvoiceReceiptPIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ApproveInvoiceUTIdent ="ApproveInvoiceUT"
  lazy val ApproveInvoiceUT = userTask(
    ApproveInvoiceUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val PrepareBankTransferUTIdent ="PrepareBankTransferUT"
  lazy val PrepareBankTransferUT = userTask(
    PrepareBankTransferUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ArchiveInvoiceSTIdent ="ArchiveInvoiceST"
  lazy val ArchiveInvoiceST = serviceTask(
    ArchiveInvoiceSTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val AssignApproverGroupBRTIdent ="AssignApproverGroupBRT"
  lazy val AssignApproverGroupBRT = // use singleEntry / collectEntries / singleResult / resultList
    dmn(
      AssignApproverGroupBRTIdent,
      in = NoInput(),
      out = NoOutput(),
      descr = None
    )

  // reviewInvoice.bpmn
  val ReviewInvoicePIdent ="ReviewInvoiceP"
  lazy val ReviewInvoiceP = process(
    ReviewInvoicePIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val AssignReviewerUTIdent ="AssignReviewerUT"
  lazy val AssignReviewerUT = userTask(
    AssignReviewerUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val ReviewInvoiceUTIdent ="ReviewInvoiceUT"
  lazy val ReviewInvoiceUT = userTask(
    ReviewInvoiceUTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )


end InvoiceDomain