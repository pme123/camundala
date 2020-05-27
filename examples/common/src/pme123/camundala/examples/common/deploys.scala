package pme123.camundala.examples.common

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy._

object deploys {

  val camundaRestUrl: Url = "http://localhost:8085/engine-rest"
  def exampleRestApi( endpoint: Url): CamundaEndpoint = CamundaEndpoint(endpoint, "kermit", Sensitive("kermit"))
  val camundaRestAPI: CamundaEndpoint = CamundaEndpoint(camundaRestUrl, "demo", Sensitive("demo"))

  def standard(bpmns: Seq[Bpmn], endpointUrl: Url, additionalUsers: Seq[User] = Seq.empty): Deploys =
    Deploys(Set(
      Deploy("default", bpmns, DockerConfig(dockerDir = "examples/docker"),
        camundaEndpoint = exampleRestApi(endpointUrl),
        additionalUsers = additionalUsers
      ),
      Deploy("remote", bpmns,
        DockerConfig(dockerDir = "examples/docker",
          composeFiles = Seq("docker-compose", "docker-compose-camunda", "docker-compose-mailcatcher"),
          maybeReadyUrl = Some(camundaRestUrl),
          projectName = "camunda-remote"
        ),
        camundaEndpoint = camundaRestAPI,
        additionalUsers = additionalUsers
      )
    ))
}
