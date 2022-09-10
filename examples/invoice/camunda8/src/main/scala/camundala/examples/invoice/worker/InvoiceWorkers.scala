package camundala.examples.invoice.worker

import camundala.examples.invoice.api.InvoiceApi.InvoiceReceipt
import io.camunda.zeebe.spring.client.annotation.{ZeebeVariablesAsType, ZeebeWorker}
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class InvoiceWorkers :

  @ZeebeWorker(`type` = "invoice-archive", autoComplete = true)
  @throws[Exception]
  def archiveInvoice(@ZeebeVariablesAsType variables: InvoiceReceipt): Unit = {
    println(s"INVOICE ARCHIVED: ${variables.invoiceNumber}")
  }


