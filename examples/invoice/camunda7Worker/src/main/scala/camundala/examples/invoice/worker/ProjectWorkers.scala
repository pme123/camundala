package camundala.examples.invoice
package worker

import camundala.examples.invoice.InvoiceReceipt
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
/*
@Configuration
class InvoiceReceiptWorker
    extends EngineWorkerDsl,
      InitProcessWorkerDsl[InvoiceReceipt.In, InvoiceReceipt.Out]:

  initProcess(InvoiceReceipt.example)

end InvoiceReceiptWorker

@Configuration
class ReviewInvoiceWorker
    extends EngineWorkerDsl,
      InitProcessWorkerDsl[ReviewInvoice.In, ReviewInvoice.Out]:
  import ReviewInvoice.*

  initProcess(ReviewInvoice.example)

  override def validate(in: In): Either[ValidatorError, In] =
    logger.info("Do some custom validation...")
    // Left(ValidatorError("bad val test"))
    Right(in)
  end validate

  override def initProcess(in: In): Either[InitProcessError, Map[String, Any]] =
    // logger.info("Do some variable initialization...")
    Right(Map("justToTestInit" -> in.amount))
  end initProcess

end ReviewInvoiceWorker

@Configuration
class ArchiveInvoiceWorker
    extends CustomWorkerDsl[ArchiveInvoice.In, ArchiveInvoice.Out],
      EngineWorkerDsl:

  import ArchiveInvoice.*

  custom(example)

  override def runWork(
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
*/