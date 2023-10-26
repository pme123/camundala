package camundala.examples.invoice
package worker

import camundala.domain.NoOutput
import camundala.worker.CamundalaWorkerError.{InitializerError, ValidatorError}
import camundala.worker.{RequestOutput, WorkerDsl}
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends WorkerDsl:

  workers(
    process(ReviewInvoice.example)
      .withCustomValidator(ReviewInvoiceWorker.customValidator)
      .withInitVariables(ReviewInvoiceWorker.initVariables),
    service(ArchiveInvoice.example)
      .withDefaultHeaders(ArchiveInvoiceWorker.defaultHeaders)
      .withBodyOutputMapper(ArchiveInvoiceWorker.bodyOutputMapper)
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
  object ArchiveInvoiceWorker:
    import ArchiveInvoice.*
    lazy val defaultHeaders: Map[String, String] = Map("crazy-header" -> "just-to-test")
    def bodyOutputMapper(requestOut: RequestOutput[ServiceOut]): Right[Nothing, Some[Out]] =
      println("Do some crazy output mapping...")
      Right(Some(Out(Some(requestOut.outputBody.nonEmpty))))
  end ArchiveInvoiceWorker

end ProjectWorkers
