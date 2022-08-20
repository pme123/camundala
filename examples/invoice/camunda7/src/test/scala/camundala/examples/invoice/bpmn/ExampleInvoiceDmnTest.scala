package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.test.*
import org.junit.Test
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
class ExampleInvoiceDmnTest extends DmnTestRunner:

  val dmnPath = baseResource / "invoiceBusinessDecisions.dmn"

  @Test
  def testSingleResult(): Unit =
    test(InvoiceAssignApproverDMN)

  @Test
  def testMoreResult(): Unit =
    test(InvoiceAssignApproverDMN2)
