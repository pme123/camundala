package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.camunda7.worker.WorkerHandler
import camundala.examples.invoice.ReviewInvoice.*
import camundala.worker.CamundalaWorkerError.{InitProcessError, ValidatorError}
import camundala.worker.InitProcessWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ReviewInvoiceWorker
  extends WorkerHandler,
    InitProcessWorkerDsl[In, Out]:

  lazy val process: bpmn.Process[In, Out] = example

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
