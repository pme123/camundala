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
                  bpmns: Seq[Bpmn],
                  dockerConfig: DockerConfig,
                  camundaEndpoint: CamundaEndpoint,
                  overwrites: Overwrites = Overwrites.none,
                  additionalUsers: Seq[User] = Seq.empty
                 ) {
  def groups(): Seq[Group] = (bpmns.flatMap(_.groups()) ++ additionalUsers.flatMap(_.groups)).distinct


  def users(): Seq[User] = (bpmns.flatMap(_.users()) ++ additionalUsers).distinct

}

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

case class Overwrites(seq: Seq[Overwrite[_]] = Seq.empty)

object Overwrites {
  val none: Overwrites = Overwrites()
}

case class Overwrite[T](from: T, to: T)

case class CamundaEndpoint(url: Url, user: Username, password: Sensitive)

