package camundala
package examples.invoice
package dmn

import bpmn.*
import domain.*
import camundala.bpmn.*
import camundala.dmn.{DmnTesterConfigCreator, DmnTesterStarter}
import org.latestbit.circe.adt.codec.JsonTaggedAdt

object InvoiceDmnTesterConfigCreator extends DmnTesterConfigCreator, App:

  override val projectBasePath: Path =
    pwd / "examples" / "invoice" / "camunda7"

  override val starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig(

  )

  run()

  dmnTester(
    InvoiceAssignApproverDMN
      .dmnPath(defaultDmnPath("invoiceBusinessDecisions"))
      .testValues("amount", 249, 250, 999, 1000, 1001)
  )





end InvoiceDmnTesterConfigCreator
