package camundala.examples.invoice.worker

import camundala.bpmn.CustomTask
import camundala.examples.invoice.ArchiveInvoice.*
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.CustomWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ArchiveInvoiceWorker 
  extends InvoiceWorkerHandler, // environment specific
    CustomWorkerDsl[In, Out]: // DSL for this type

  lazy val customTask = example

  def runWork(
      inputObject: In,
  ): Either[CustomError, Out] =
    logger.info("Do some crazy things running work...")
    inputObject.shouldFail match
      case Some(false) =>
        Right(Out(Some(true)))
      case Some(true) =>
        val err = CustomError("Could not archive invoice...")
        logger.error(err)
        Left(err)
      case _ =>
        Right(Out(Some(false)))

end ArchiveInvoiceWorker
