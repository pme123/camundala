package camundala.examples.invoice
package listener

import camundala.camunda.InputHandler
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class InvoiceInputHandler extends InputHandler[InvoiceReceipt.In] :

  lazy val prototype: InvoiceReceipt.In = InvoiceReceipt.In()
