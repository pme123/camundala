package camundala.examples.invoice
package worker

import camundala.worker.CamundalaWorkerError.ValidatorError
import camundala.worker.WorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends WorkerDsl:

  register(
    process(ReviewInvoice.example)
      .withCustomValidator(ReviewInvoiceWorker.customValidator),
    service(ArchiveInvoice.example)
  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    def customValidator(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      Right(in)
  end ReviewInvoiceWorker

end ProjectWorkers
