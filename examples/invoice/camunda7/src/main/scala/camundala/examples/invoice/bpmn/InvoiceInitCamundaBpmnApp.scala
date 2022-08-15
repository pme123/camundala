package camundala.examples.invoice.bpmn

import camundala.camunda.InitCamundaBpmn
import os.pwd

object InvoiceInitCamundaBpmnApp extends InitCamundaBpmn, App:

  val projectPath = pwd / "examples" / "invoice"

  run("Invoice")

end InvoiceInitCamundaBpmnApp


