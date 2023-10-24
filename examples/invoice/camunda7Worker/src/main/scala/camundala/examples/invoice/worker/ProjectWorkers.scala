package camundala.examples.invoice
package worker

import camundala.worker.WorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends WorkerDsl:

  register(
    worker(InvoiceReceipt.example),
   // worker(ArchiveInvoice.example)
  )
end ProjectWorkers
