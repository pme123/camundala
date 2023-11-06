package camundala.examples.invoice.worker

import camundala.bpmn.CustomTask
import camundala.examples.invoice.ArchiveInvoice.*
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.CustomWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ArchiveInvoiceWorker 
  extends WorkerHandler, // environment specific
    CustomWorkerDsl[In, Out]: // DSL for this type

  lazy val customTask = example

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
