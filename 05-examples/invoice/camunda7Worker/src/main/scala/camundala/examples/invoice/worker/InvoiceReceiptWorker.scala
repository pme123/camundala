package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.examples.invoice.InvoiceReceipt.*
import camundala.worker.InitWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker extends InvoiceWorkerHandler, InitWorkerDsl[In, Out]:

  lazy val inOut = example

end InvoiceReceiptWorker
