package camundala.examples.invoice.worker

import camundala.domain.Process
import camundala.examples.invoice.bpmn.ReviewInvoice.*
import camundala.worker.CamundalaWorkerError.ValidatorError
import camundala.worker.InitWorkerDsl
import org.springframework.context.annotation.Configuration

@Configuration
class ReviewCompanyWorker extends CompanyInitWorkerDsl[In, Out, InitIn, InConfig]:

  lazy val inOutExample: Process[In, Out, InitIn] = example

  override def validate(in: In): Either[ValidatorError, In] =
    logger.info(s"Do some custom validation...")
    logger.info(s"- $in")
    // Left(ValidatorError("bad val test"))
    Right(in)
  end validate

  override def customInit(in: In): InitIn =
    InitIn(
      in.creditor,
      in.amount,
      in.invoiceCategory,
      in.invoiceNumber,
      justToTestInit = in.amount / 2
    )
  end customInit

end ReviewCompanyWorker
