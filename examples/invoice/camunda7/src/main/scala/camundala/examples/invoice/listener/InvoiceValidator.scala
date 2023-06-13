package camundala.examples.invoice
package listener

import camundala.camunda.Validator
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class InvoiceValidator extends Validator[InvoiceReceipt.In] :

  lazy val product: InvoiceReceipt.In = InvoiceReceipt.In()
