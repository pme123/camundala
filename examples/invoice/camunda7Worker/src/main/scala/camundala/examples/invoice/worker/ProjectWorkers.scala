package camundala.examples.invoice
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.*
import org.springframework.context.annotation.Configuration

@Configuration
class ProjectWorkers extends EngineWorkerDsl:
  lazy val logger = engineContext.getLogger(getClass)
  workers(
    initProcess(InvoiceReceipt.example),
    initProcess(ReviewInvoice.example)
      //.validation(ReviewInvoiceWorker.customValidator)
      .validation(ReviewInvoiceWorker.validate) // implicit conversion
      .initProcess(ReviewInvoiceWorker.initVariables),
    custom(ArchiveInvoice.example)
      .runWork(ArchiveInvoiceWorker.runWork)
  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    lazy val customValidator = ValidationHandler(validate)
    def validate(in: In): Either[ValidatorError, In] =
      logger.info("Do some custom validation...")
      // Left(ValidatorError("bad val test"))
      Right(in)

    def initVariables(in: In): Either[InitProcessError, Map[String, Any]] =
      logger.info("Do some variable initialization...")
      Right(Map("justToTestInit" -> true))
    end initVariables

  end ReviewInvoiceWorker

  object ArchiveInvoiceWorker:
    import ArchiveInvoice.*

    def runWork(
        inputObject: In,
        optOutput: Option[Out]
    ): Either[CustomError, Option[Out]] =
      logger.info("Do some crazy things running work...")
      inputObject.shouldFail match
        case Some(false) =>
          Right(Some(Out(Some(true))))
        case Some(true) =>
          val err = CustomError("Could not archive invoice...")
          logger.error(err)
          Left(err)
        case _ =>
          Right(Some(Out(Some(false))))

  end ArchiveInvoiceWorker
  
end ProjectWorkers
