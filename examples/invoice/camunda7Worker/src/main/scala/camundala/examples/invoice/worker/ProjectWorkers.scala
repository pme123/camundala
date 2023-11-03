package camundala.examples.invoice
package worker

import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration

@Configuration
class InvoiceReceiptWorker extends EngineWorkerDsl:

  lazy val worker =
    initProcess(InvoiceReceipt.example)

end InvoiceReceiptWorker

@Configuration
class ReviewInvoiceWorker extends EngineWorkerDsl:
  import ReviewInvoice.*

  lazy val worker =
    initProcess(ReviewInvoice.example)
      // .validation(ReviewInvoiceWorker.customValidator)
      .validation(validate) // implicit conversion
      .initProcess(initVariables)

  lazy val customValidator = ValidationHandler(validate)

  def validate(in: In): Either[ValidatorError, In] =
    logger.info("Do some custom validation...")
    // Left(ValidatorError("bad val test"))
    Right(in)
  end validate

  def initVariables(in: In): Either[InitProcessError, Map[String, Any]] =
    //logger.info("Do some variable initialization...")
    Right(Map("justToTestInit" -> in.amount))
  end initVariables
end ReviewInvoiceWorker

@Configuration
class ArchiveInvoiceWorker extends EngineWorkerDsl:
  import ArchiveInvoice.*

  lazy val worker =
    custom(example)
      .runWork(runWork)

  private def runWork(
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
