
import eu.timepit.refined.auto._
import pme123.camundala.examples.playground.ChangeAddressBpmn
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.deploy.{CamundaEndpoint, Deploy, Deploys, DockerConfig}
import pme123.camundala.model.scenarios.ProcessScenario


val bpmn = ChangeAddressBpmn().ChangeAddressBpmn
val process = bpmn.processes.head
lazy val devDeploy =
  Deploy()
    .---(/*playgroundBpmn,*/ bpmn)
    .---(DockerConfig.DefaultDevConfig.dockerDir("examples/docker"))
    .---(ProcessScenario("Happy Path", process))
    .addUsers(heidi, kermit, adminUser)

lazy val remoteDeploy =
  devDeploy
    .id("remote")
    .---(CamundaEndpoint.DefaultRemoteEndpoint)
    .---(DockerConfig.DefaultRemoteConfig.dockerDir("examples/docker"))

Deploys()
  .+++(devDeploy)
  .+++(remoteDeploy)
