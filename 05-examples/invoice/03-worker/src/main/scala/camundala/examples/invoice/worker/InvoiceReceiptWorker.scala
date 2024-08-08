package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.examples.invoice.bpmn.InvoiceReceipt.*
import camundala.worker.InitWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:

  lazy val inOutExample = example
  override protected def customInit(in: In): InitIn = in

end InvoiceReceiptWorker
