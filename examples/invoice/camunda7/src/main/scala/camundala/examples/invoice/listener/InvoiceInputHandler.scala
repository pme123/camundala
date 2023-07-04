package camundala.examples.invoice
package listener

import camundala.camunda.InputHandler

class InvoiceInputHandler extends InputHandler[InvoiceReceipt.In] :

  lazy val prototype: InvoiceReceipt.In = InvoiceReceipt.In()
