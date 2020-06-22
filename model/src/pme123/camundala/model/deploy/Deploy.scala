package pme123.camundala.model.deploy

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.scenarios.ProcessScenario

case class Deploys(value: Set[Deploy] = Set.empty) {
  def deploy(depl: Deploy): Deploys = copy(value = value + depl)

  def +++(depl: Deploy): Deploys = deploy(depl)
}

object Deploys {
}

/**
  *
  * @param id           The Id of the deployment - must be unique in the deployRegistry
  * @param bpmns        Bpmns you want to have in this Deployment (send together to Camunda)
  * @param dockerConfig Configures the Docker
  */
case class Deploy(id: DeployId = "default",
                  bpmns: Seq[Bpmn] = Seq.empty,
                  dockerConfig: DockerConfig = DockerConfig.DefaultDevConfig,
                  camundaEndpoint: CamundaEndpoint = CamundaEndpoint.DefaultDevEndpoint,
                  additionalUsers: Seq[User] = Seq.empty,
                  scenarios: Seq[ProcessScenario] = Seq.empty
                 ) {

  def id(ident: DeployId): Deploy = copy(id = ident)

  def groups(): Seq[Group] = (bpmns.flatMap(_.groups()) ++ additionalUsers.flatMap(_.groups)).distinct


  def users(): Seq[User] = (bpmns.flatMap(_.users()) ++ additionalUsers).distinct

  def bpmns(bpmnModels: Bpmn*): Deploy = copy(bpmns = bpmns ++ bpmnModels)

  def bpmn(bpmnModel: Bpmn): Deploy = bpmns(bpmnModel)

  def ---(bpmnModel: Bpmn): Deploy = bpmns(bpmnModel)

  def dockerConfig(dockerC: DockerConfig): Deploy = copy(dockerConfig = dockerC)

  def ---(dockerC: DockerConfig): Deploy = dockerConfig(dockerC)

  def endpoint(endp: CamundaEndpoint): Deploy = copy(camundaEndpoint = endp)

  def ---(endp: CamundaEndpoint): Deploy = endpoint(endp)

  def addUsers(users: User*): Deploy = copy(additionalUsers = additionalUsers ++ users)

  def addUser(user: User): Deploy = addUsers(user)

  def ---(user: User): Deploy = addUsers(user)

  def scenarios(processScenarios: ProcessScenario*): Deploy = copy(scenarios = scenarios ++ processScenarios)

  def scenario(processScenario: ProcessScenario): Deploy = scenarios(processScenario)

  def ---(processScenario: ProcessScenario): Deploy = scenarios(processScenario)

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

  def dockerDir(filePath: FilePath): DockerConfig = copy(dockerDir = filePath)

  def composeFiles(filePaths: FilePath*): DockerConfig = copy(composeFiles = composeFiles ++ filePaths)

  def composeFile(filePath: FilePath): DockerConfig = composeFiles(filePath)
}

object DockerConfig {
  val DefaultDevConfig: DockerConfig =
    DockerConfig("camundala-default-dev")
      .composeFile("docker-compose-dev")
  val DefaultRemoteConfig: DockerConfig = DockerConfig()

}


case class CamundaEndpoint(url: Url, user: Username, password: Sensitive)

object CamundaEndpoint {
  val DefaultRemoteUrl: Url = "http://localhost:8085/rest"
  val DefaultDevUrl: Url = "http://localhost:8088/rest"
  val DefaultRemoteEndpoint: CamundaEndpoint = restApi(DefaultRemoteUrl)
  val DefaultDevEndpoint: CamundaEndpoint = restApi(DefaultDevUrl)

  def restApi(endpoint: Url): CamundaEndpoint = CamundaEndpoint(endpoint, "kermit", Sensitive("kermit"))

}

