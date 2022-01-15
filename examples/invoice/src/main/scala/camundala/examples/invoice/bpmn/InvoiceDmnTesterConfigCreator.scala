package camundala
package examples.invoice.bpmn

import bpmn.*
import dmn.DmnTesterConfigCreator
import org.latestbit.circe.adt.codec.JsonTaggedAdt

object InvoiceDmnTesterConfigCreator extends DmnTesterConfigCreator:

  import InvoiceApi.*
  override def dmnBasePath: Path =
    pwd / "examples" / "invoice" / "src" / "main" / "resources"

  dmnTester(
    invoiceAssignApproverDMN.tester
      .dmnPath(defaultDmnPath("invoiceBusinessDecisions"))
      .testValues("amount", 249, 250, 999, 1000, 1001)
  )
