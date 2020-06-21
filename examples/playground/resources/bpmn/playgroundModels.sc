
import eu.timepit.refined.auto._
import pme123.camundala.examples.playground.ChangeAddressBpmn
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.deploy.Deploys



Deploys.standard(Seq(/*playgroundBpmn,*/ ChangeAddressBpmn().ChangeAddressBpmn),
  Seq(heidi, kermit, adminUser),
  "examples/docker")
