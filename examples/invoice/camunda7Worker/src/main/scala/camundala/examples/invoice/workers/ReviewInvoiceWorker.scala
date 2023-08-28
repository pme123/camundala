package camundala.examples.invoice
package workers

import camundala.camunda7.worker.*
import camundala.examples.invoice.ReviewInvoice.*
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
import org.springframework.context.annotation.Configuration

@Configuration
@ExternalTaskSubscription(
  value = processName
)
class ReviewInvoiceWorker
    extends CamundalaWorker[In, Out]:

  val isService: Boolean = false
  protected def processName: String = processName

  lazy val prototype = In()
  lazy val defaultMock = Out()

end ReviewInvoiceWorker
