package camundala.examples.invoice.worker

import camundala.examples.invoice.domain.InvoiceReceipt
import io.camunda.zeebe.spring.client.annotation.{JobWorker, VariablesAsType}
import org.springframework.stereotype.Component

@Component
class InvoiceWorkers :

  @JobWorker(`type` = "invoice-archive", autoComplete = true)
  @throws[Exception]
  def archiveInvoice(@VariablesAsType variables: InvoiceReceipt): Unit = {
    println(s"INVOICE ARCHIVED: ${variables.invoiceNumber}")
  }


