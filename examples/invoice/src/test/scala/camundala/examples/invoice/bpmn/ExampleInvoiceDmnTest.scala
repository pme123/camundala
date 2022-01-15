package camundala.examples.invoice.bpmn

import camundala.examples.invoice.bpmn.InvoiceApi.*
import camundala.test.*
import org.junit.Test

class ExampleInvoiceDmnTest extends DmnTestRunner:

  val dmnPath = baseResource / "invoiceBusinessDecisions.dmn"

  @Test
  def testSingleResult(): Unit =
    test(invoiceAssignApproverDMN)

  @Test
  def testMoreResult(): Unit =
    test(invoiceAssignApproverDMN2)
