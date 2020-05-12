package pme123.camundala.model.deploy

import java.nio.file.{Path, Paths}

import pme123.camundala.model.bpmn.Bpmn

case class Deploys(value: Set[Deploy])
/**
  *
  * @param id The Id of the deployment - must be unique in the deployRegistry
  * @param bpmns Bpmns you want to have in this Deployment (send together to Camunda)
  */
case class Deploy(id: String,
                  bpmns: Set[Bpmn],
                  dockerConfig: DockerConfig)

case class DockerConfig(projectName: String = "camundala-default",
                        composeFiles: Seq[String] = List("docker-compose"),
                        dockerDir: Path = Paths.get(s"./docker"),
                        maybeReadyUrl: Option[String] = None,
                       ) {

  def withProjectDir(projectBaseDir: String): DockerConfig =
    copy(dockerDir = Paths.get(s"$projectBaseDir/docker"))

  def composeFilesString(): String =
    composeFiles
      .map(f => s"-f ${s"$dockerDir/$f.yml"}")
      .mkString(" ")
}
