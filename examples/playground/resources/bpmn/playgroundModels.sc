
import eu.timepit.refined.auto._
import pme123.camundala.examples.playground.ChangeAddressBpmn
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.deploy.{CamundaEndpoint, Deploy, Deploys, DockerConfig}


lazy val devDeploy =
  Deploy()
    .---(/*playgroundBpmn,*/ ChangeAddressBpmn().ChangeAddressBpmn)
    .---(DockerConfig.DefaultDevConfig.dockerDir("examples/docker"))
    .addUsers(heidi, kermit, adminUser)

lazy val remoteDeploy =
  devDeploy
    .id("remote")
    .---(CamundaEndpoint.DefaultRemoteEndpoint)
    .---(DockerConfig.DefaultRemoteConfig.dockerDir("examples/docker"))


Deploys()
  .+++(devDeploy)
  .+++(remoteDeploy)
