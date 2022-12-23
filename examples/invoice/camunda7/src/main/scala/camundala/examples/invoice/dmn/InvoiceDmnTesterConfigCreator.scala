package camundala
package examples.invoice
package dmn

import bpmn.*
import domain.*
import camundala.bpmn.*
import camundala.dmn.DmnTesterConfigCreator
import org.latestbit.circe.adt.codec.JsonTaggedAdt

object InvoiceDmnTesterConfigCreator extends DmnTesterConfigCreator:

  override def dmnBasePath: Path =
    pwd / "examples" / "invoice" / "src" / "main" / "resources"

  dmnTester(
    InvoiceAssignApproverDMN.tester
      .dmnPath(defaultDmnPath("invoiceBusinessDecisions"))
      .testValues("amount", 249, 250, 999, 1000, 1001)
  )
