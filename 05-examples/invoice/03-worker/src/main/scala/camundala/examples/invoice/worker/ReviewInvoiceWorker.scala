package camundala.examples.invoice.worker

import camundala.bpmn
import camundala.examples.invoice.bpmn.ReviewInvoice.*
import camundala.worker.CamundalaWorkerError.{InitProcessError, ValidatorError}
import camundala.worker.{GeneralVariables, InitWorkerDsl}
import org.springframework.context.annotation.Configuration

@Configuration
class ReviewInvoiceWorker extends InvoiceWorkerHandler, InitWorkerDsl[In, Out, InConfig]:

  lazy val inOutExample: bpmn.Process[In, Out] = example
  
  override def validate(in: In): Either[ValidatorError, In] =
    logger.info(s"Do some custom validation...")
    logger.info(s"- $in")
    // Left(ValidatorError("bad val test"))
    Right(in)
  end validate

  override def initProcess(in: In): Either[InitProcessError, Map[String, Any]] =
    // logger.info("Do some variable initialization...")
    super.initProcess(in).map(_ + ("justToTestInit" -> in.amount))
  end initProcess

end ReviewInvoiceWorker
