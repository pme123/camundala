package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import io.circe.parser._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.{MockData, Request}
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.bpmn.{BpmnNodeId, ServiceTask}

case class AddressService(addressHost: Host = Host.unknown) {


  val maybeMockData: Option[MockData] =
    parse(
      """{
        |	    "id": "362350000000135",
        |		  "street": "Murtengasse 22",
        |		  "zipCode": "3600",
        |		  "city": "Thun",
        |		  "countryIso": "CH"
        |}""".stripMargin)
      .toOption.map(MockData(200, _))

  def getAddress(id: BpmnNodeId): ServiceTask =
    RestServiceTempl(
      Request(
        addressHost,
        path = Path("_customerId_", "_addressType_"),
        responseVariable = "existingAddress",
        mappings = Map("_customerId_" -> "dummyCustomerId", "_addressType_" -> "11"),
        maybeMocked = if (addressHost == Host.unknown) maybeMockData else None
      )
    ).asServiceTask(id)

}
