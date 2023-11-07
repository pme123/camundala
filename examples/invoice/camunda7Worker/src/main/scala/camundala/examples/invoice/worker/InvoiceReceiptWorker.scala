package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.examples.invoice.InvoiceReceipt.*
import camundala.worker.InitProcessWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker extends InvoiceWorkerHandler, InitProcessWorkerDsl[In, Out]:

  lazy val process = example

end InvoiceReceiptWorker
