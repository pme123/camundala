package camundala.examples.invoice
package worker

import camundala.worker.CamundalaWorkerError.{InitializerError, ValidatorError}
import camundala.worker.WorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends WorkerDsl:

  register(
    process(ReviewInvoice.example)
      .withCustomValidator(ReviewInvoiceWorker.customValidator)
      .withInitVariables(ReviewInvoiceWorker.initVariables),
    service(ArchiveInvoice.example)
  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    def customValidator(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      Right(in)
    end customValidator

    def initVariables(in: In): Either[InitializerError, Map[String, Any]] =
      println("Do some variable initialization...")
      Right(Map("justToTestInit" -> true))
    end initVariables

  end ReviewInvoiceWorker

end ProjectWorkers
