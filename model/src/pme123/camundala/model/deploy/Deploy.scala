package pme123.camundala.model.deploy

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn._

case class Deploys(value: Set[Deploy])

/**
  *
  * @param id           The Id of the deployment - must be unique in the deployRegistry
  * @param bpmns        Bpmns you want to have in this Deployment (send together to Camunda)
  * @param dockerConfig Configures the Docker
  */
case class Deploy(id: DeployId,
                  bpmns: Set[Bpmn],
                  dockerConfig: DockerConfig,
                  maybeRemote: Option[CamundaEndpoint] = None,
                 )

case class DockerConfig(projectName: ProjectName = "camundala-default",
                        composeFiles: Seq[FilePath] = List("docker-compose"),
                        dockerDir: FilePath = "docker",
                        maybeReadyUrl: Option[Url] = None,
                       ) {

  def composeFilesString(): String =
    composeFiles
      .map(f => s"-f ${s"$dockerDir/$f.yml"}")
      .mkString(" ")
}

case class CamundaEndpoint(url: Url, user: Username, password: Sensitive)

