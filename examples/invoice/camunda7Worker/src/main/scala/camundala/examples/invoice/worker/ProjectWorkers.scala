package camundala.examples.invoice
package worker

import camundala.domain.NoOutput
import camundala.worker.CamundalaWorkerError.{InitializerError, ServiceUnexpectedError, ValidatorError}
import camundala.worker.{EngineWorkerDsl, RequestOutput}
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends EngineWorkerDsl:

  workers(
    process(ReviewInvoice.example)
      .withCustomValidator(ReviewInvoiceWorker.customValidator)
      .withInitVariables(ReviewInvoiceWorker.initVariables),
    service(ArchiveInvoice.example)
      .withDefaultHeaders(ArchiveInvoiceWorker.defaultHeaders)
      .withBodyOutputMapper(ArchiveInvoiceWorker.bodyOutputMapper)
      .withWorkRunner(ArchiveInvoiceWorker.runWork)
  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    def customValidator(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      // Left(ValidatorError("bad val test"))
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

    def runWork(inputObject: In, optOutput: Option[Out]): Either[ServiceUnexpectedError, Option[Out]] =
      println("Do some crazy things running work...")
      optOutput match
        case Some(out) =>
          Right(Some(out))
        case None if inputObject.shouldFail.getOrElse(false) =>
          Left(ServiceUnexpectedError("Could not archive invoice..."))
        case _ =>
          Right(Some(Out()))

  end ArchiveInvoiceWorker

end ProjectWorkers
