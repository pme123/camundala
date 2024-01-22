package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.examples.invoice.bpmn.InvoiceReceipt.*
import camundala.worker.InitWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker extends InvoiceWorkerHandler, InitWorkerDsl[In, Out, InConfig]:

  lazy val inOutExample = example

end InvoiceReceiptWorker
