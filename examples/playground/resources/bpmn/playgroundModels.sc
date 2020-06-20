
import eu.timepit.refined.auto._
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.examples.playground.{AddressService, ChangeAddressBpmn}
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint.Required
import pme123.camundala.model.bpmn.UserTaskForm.FormField._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Deploys



Deploys.standard(Seq(/*playgroundBpmn,*/ ChangeAddressBpmn().addressBpmn),
  Seq(heidi, kermit, adminUser),
  "examples/docker")
