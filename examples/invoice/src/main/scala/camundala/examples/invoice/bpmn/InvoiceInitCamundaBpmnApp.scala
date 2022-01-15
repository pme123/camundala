package camundala
package examples.invoice.bpmn

import camunda.*
import os.pwd

object InvoiceInitCamundaBpmnApp extends InitCamundaBpmn:

  val projectPath = pwd / "examples" / "invoice"

  run("Invoice")

end InvoiceInitCamundaBpmnApp


