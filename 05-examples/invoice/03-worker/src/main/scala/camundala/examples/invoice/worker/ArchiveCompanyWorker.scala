package camundala.examples.invoice.worker

import camundala.domain.CustomTask
import camundala.examples.invoice.bpmn.ArchiveInvoice.*
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.*
import org.springframework.context.annotation.Configuration

@Configuration
class ArchiveCompanyWorker
    extends CompanyCustomWorkerDsl[In, Out]: // DSL for this type

  lazy val customTask = example

  override def runWork(
      inputObject: In
  ): Either[CustomError, Out] =
    logger.info("Do some crazy things running work...")
    inputObject.shouldFail match
      case Some(false) =>
        Right(Out(Some(true)))
      case Some(true)  =>
        val err = CustomError("Could not archive invoice...")
        logger.error(err)
        Left(err)
      case _           =>
        Right(Out(Some(false)))
    end match
  end runWork

end ArchiveCompanyWorker
