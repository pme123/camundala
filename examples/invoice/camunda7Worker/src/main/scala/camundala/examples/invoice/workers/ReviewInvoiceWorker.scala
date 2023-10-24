package camundala.examples.invoice
package workers

import camundala.camunda7.worker.*
import camundala.examples.invoice.ReviewInvoice.*
import org.springframework.context.annotation.Configuration

@Configuration
class ReviewInvoiceWorker
    extends CamundalaWorker[In, Out]:

  lazy val topic: String = processName
  val isService: Boolean = false
  //protected def processName: String = processName

  lazy val prototype = In()
  lazy val defaultMock = Out()

end ReviewInvoiceWorker
