package camundala.examples.invoice
package listener

import camundala.camunda.Validator
import bpmn.InvoiceApi.InvoiceReceipt
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class InvoiceValidator extends Validator[InvoiceReceipt] :

  val product: InvoiceReceipt = InvoiceReceipt()
