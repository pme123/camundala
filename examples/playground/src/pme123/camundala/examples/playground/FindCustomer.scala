package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import io.circe.parser.parse
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.{MockData, Request}
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm._
import pme123.camundala.model.bpmn.{Bpmn, BpmnNodeId, BpmnProcess, PropKey, SequenceFlow, ServiceTask, StartEvent, UserTaskForm}
import pme123.camundala.model.bpmn.ops._

case class FindCustomer(addressHost: Host = Host.unknown) {

  import FindCustomer._

  lazy val bpmn: Bpmn = Bpmn("find-customer.bpmn", "find-customer.bpmn")
    .###(process)

  lazy val process: BpmnProcess = BpmnProcess(findUserProcessId)
    .starterGroup(UsersAndGroups.adminGroup)
    .***(startEvent)
    .serviceTask(findCustomer)

  lazy val startEvent: StartEvent =
    StartEvent("StartEvent_find-customer")
      .===(GeneratedForm()
        .---(textField(lastname).required)
        .---(textField(firstname))
        .---(dateField(birthday))
      )

  lazy val findCustomer: ServiceTask =
    RestServiceTempl(
      Request(
        addressHost,
        path = Path("customer", "tobedefined"),
        responseVariable = foundCustomers,
      //  mappings = Map(lastname.value -> "", firstname.value -> "", birthday.value -> ""),
        maybeMocked = if (addressHost == Host.unknown) maybeMockData else None
      )
    ).asServiceTask("find-customer-task")

  lazy val maybeMockData: Option[MockData] =
    parse(
      """[{
        |	    "clientKey": "444350000",
        |		  "name": "Gerber",
        |		  "firstName": "Heidi",
        |		  "streetNo": "Wonner 45",
        |     "postcodeWithPlace": "6565 Allalie"
        |},{
        |	    "clientKey": "362350000",
        |		  "name": "Meier",
        |		  "firstName": "Peter",
        |		  "streetNo": "Sonnenallee 45",
        |     "postcodeWithPlace": "4444 Oberrüti"
        |}]""".stripMargin)
      .toOption.map(MockData(200, _))
}

object FindCustomer {
  val findUserProcessId: PropKey = "find-customer-process"
  val lastname: PropKey = "lastname"
  val firstname: PropKey = "firstname"
  val birthday: PropKey = "birthday"
  val foundCustomers: PropKey = "foundCustomers"
}
