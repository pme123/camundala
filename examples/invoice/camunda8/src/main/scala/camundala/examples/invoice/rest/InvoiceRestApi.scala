package camundala.examples.invoice
package rest

import api.*
import api.InvoiceApi.*
import camundala.bpmn.*
import camundala.camunda8.*
import io.circe
import io.circe.parser.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.*

@RestController
class InvoiceRestApi extends RestEndpoint:

  @PostMapping( value = Array("/process-definition/InvoiceReceiptP/create"))
  def createTweetReviewProcess(
      @RequestBody
      json: String
  ): Response =
    createInstance(
      InvoiceReceiptPIdent,
      validate[CreateProcessInstanceIn[InvoiceReceipt, InvoiceReceiptCheck]](
        json
      ).map(_.syncProcess(classOf[InvoiceReceiptCheck]))
    )

