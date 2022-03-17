package camundala.examples.demos

import camundala.bpmn.*
import camundala.api.*
import sttp.tapir.json.circe.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object MappingDomain extends BpmnDsl:

  case class MappingExampleIn(
                               clientId: Int = 1234,
                               docData: DocData = DocData()
                             )
  case class DocData(important: String = "medium")

  case class MappingExampleOut(
      @description("For testing: check if Printing was a success")
      printSucceeded: Boolean = true
  )

  case class AddressServiceIn(customer: Int = 1234)

  case class AddressServiceOut(street: String = "Regenbogenallee", streetNr: String = "23a", zipcode: Int = 7800, place: String = "St. Gallen")

  val CamundalaMappingExampleIdent = "camundala-mapping-example"
  lazy val CamundalaMappingExample = process(
    CamundalaMappingExampleIdent,
    in = MappingExampleIn(),
    out = MappingExampleOut(),
    descr = None
  )

  val GetAddressCAIdent = "GetAddressCA"
  lazy val GetAddressCA = callActivity(
    GetAddressCAIdent,
    in = AddressServiceIn(),
    out = AddressServiceOut(),
    descr = None
  )

  val PrintDocumentCAIdent = "PrintDocumentCA"
  lazy val PrintDocumentCA = callActivity(
    PrintDocumentCAIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val SucceededEEIdent = "SucceededEE"
  lazy val SucceededEE = endEvent(
    SucceededEEIdent,
    descr = None
  )

  val NotSucceededEEIdent = "NotSucceededEE"
  lazy val NotSucceededEE = endEvent(
    NotSucceededEEIdent,
    descr = None
  )

  val PrintedEEIdent = "PrintedEE"
  lazy val PrintedEE = endEvent(
    PrintedEEIdent,
    descr = None
  )

  val CamundalaAddressServiceIdent = "camundala-address-service"
  lazy val CamundalaAddressService = process(
    CamundalaAddressServiceIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val GetAddressSTIdent = "GetAddressST"
  lazy val GetAddressST = serviceTask(
    GetAddressSTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val PrintSTIdent = "PrintST"
  lazy val PrintST = serviceTask(
    PrintSTIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )

  val AddressFetchedEEIdent = "AddressFetchedEE"
  lazy val AddressFetchedEE = endEvent(
    AddressFetchedEEIdent,
    descr = None
  )
  val CamundalaPrintServiceIdent = "camundala-print-service"
  lazy val CamundalaPrintService = process(
    CamundalaPrintServiceIdent,
    in = NoInput(),
    out = NoOutput(),
    descr = None
  )
