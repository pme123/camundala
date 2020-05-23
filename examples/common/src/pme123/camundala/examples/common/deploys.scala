package pme123.camundala.examples.common
import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.Bpmn
import pme123.camundala.model.deploy._

object deploys {

  val camundaRestUrl: Url = "http://localhost:8085/engine-rest"
  val camundaRestAPI: Option[CamundaEndpoint] = Some(CamundaEndpoint(camundaRestUrl, "demo", Sensitive("demo")))

  def standard(bpmns: Set[Bpmn]): Deploys =
   Deploys(Set(
     Deploy("default", bpmns, DockerConfig(dockerDir = "examples/docker")),
     Deploy("remote", bpmns, DockerConfig(dockerDir = "examples/docker",
       composeFiles = Seq("docker-compose", "docker-compose-camunda", "docker-compose-mailcatcher"),
       maybeReadyUrl = Some(camundaRestUrl),
       projectName = "camunda-remote"),
       maybeRemote = camundaRestAPI
     )
   ))
}
