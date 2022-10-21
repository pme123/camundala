package camundala.examples.invoice
package rest

import api.*
import bpmn.*
import domain.*
import camundala.domain.*
import camundala.api.*
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
      )//not sync: .map(_.syncProcess(classOf[InvoiceReceiptCheck]))
    )

