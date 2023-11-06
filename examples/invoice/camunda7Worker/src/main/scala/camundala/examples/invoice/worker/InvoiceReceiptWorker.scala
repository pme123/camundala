package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.camunda7.worker.EngineWorkerDsl
import camundala.examples.invoice.InvoiceReceipt.*
import camundala.worker.InitProcessWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker
  extends EngineWorkerDsl,
    InitProcessWorkerDsl[In, Out]:

  lazy val process: bpmn.Process[In, Out] = example

end InvoiceReceiptWorker