package camundala.examples.invoice
package rest

import bpmn.*
import camundala.camunda8.*
import org.springframework.web.bind.annotation.*

@RestController
class InvoiceRestApi extends RestEndpoint:

  @PostMapping(value = Array("/process-definition/InvoiceReceiptP/create"))
  def createTweetReviewProcess(
      @RequestBody
      json: String
  ): Response =
    createInstance(
      InvoiceReceipt.processName,
      validate[CreateProcessInstanceIn[InvoiceReceipt.In, InvoiceReceipt.Out]](
        json
      ) // not sync: .map(_.syncProcess(classOf[InvoiceReceiptCheck]))
    )
end InvoiceRestApi
