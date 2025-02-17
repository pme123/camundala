package camundala.helper.dev.company

import camundala.helper.dev.update.createIfNotExists
import camundala.helper.util.*

case class CompanyWrapperGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createIfNotExists(projectBpmnPath, bpmnWrapper)
    createIfNotExists(projectApiPath, apiWrapper)
    createIfNotExists(projectDmnPath, dmnWrapper)
    createIfNotExists(projectSimulationPath, simulationWrapper)
    createIfNotExists(projectWorkerHandlerPath, workerHandlerWrapper)
    createIfNotExists(projectWorkerContextPath, workerContextWrapper)
    createIfNotExists(projectWorkerPasswordPath, workerPasswordWrapper)
    createIfNotExists(projectWorkerRestApiPath, workerRestApiWrapper)
    createIfNotExists(projectWorkerConfigPath, workerApplicationConfig)
    createIfNotExists(helperCompanyDevHelperPath, helperCompanyDevHelperWrapper)
    createIfNotExists(helperCompanyDevConfigPath, helperCompanyDevConfigWrapper)
    createIfNotExists(helperCompanyCamundalaDevHelperPath, helperCompanyCamundalaDevHelperWrapper)
  end generate

  private lazy val companyName = config.companyName

  private lazy val projectBpmnPath                     = ModuleConfig.bpmnModule.srcPath / "CompanyBpmnDsl.scala"
  private lazy val projectApiPath                      = ModuleConfig.apiModule.srcPath / "CompanyApiCreator.scala"
  private lazy val projectDmnPath                      = ModuleConfig.dmnModule.srcPath / "CompanyDmnTester.scala"
  private lazy val projectSimulationPath               =
    ModuleConfig.simulationModule.srcPath / "CompanySimulation.scala"
  private lazy val projectWorkerHandlerPath            =
    ModuleConfig.workerModule.srcPath / "CompanyWorkerHandler.scala"
  private lazy val projectWorkerContextPath            =
    ModuleConfig.workerModule.srcPath / "CompanyEngineContext.scala"
  private lazy val projectWorkerPasswordPath           =
    ModuleConfig.workerModule.srcPath / "CompanyPasswordFlow.scala"
  private lazy val projectWorkerRestApiPath            =
    ModuleConfig.workerModule.srcPath / "CompanyRestApiClient.scala"
  private lazy val projectWorkerConfigPath             =
    ModuleConfig.workerModule.resourcePath / "application-company-defaults.yaml"
  private lazy val helperCompanyDevHelperPath          =
    ModuleConfig.helperModule.srcPath / "CompanyDevHelper.scala"
  private lazy val helperCompanyDevConfigPath          =
    ModuleConfig.helperModule.srcPath / "CompanyDevConfig.scala"
  private lazy val helperCompanyCamundalaDevHelperPath =
    ModuleConfig.helperModule.srcPath / "CompanyCamundalaDevHelper.scala"

  private lazy val bpmnWrapper =
    s"""package $companyName.camundala.bpmn
       |
       |/**
       | * Add here company specific stuff, like documentation or custom elements.
       | */
       |trait CompanyBpmnDsl:
       |  // override def companyDescr = ??? //TODO Add your specific Company Description!
       |end CompanyBpmnDsl
       |
       |trait CompanyBpmnProcessDsl extends BpmnProcessDsl, CompanyBpmnDsl
       |trait CompanyBpmnServiceTaskDsl extends BpmnServiceTaskDsl, CompanyBpmnDsl
       |trait CompanyBpmnCustomTaskDsl extends BpmnCustomTaskDsl, CompanyBpmnDsl
       |trait CompanyBpmnDecisionDsl extends BpmnDecisionDsl, CompanyBpmnDsl
       |trait CompanyBpmnUserTaskDsl extends BpmnUserTaskDsl, CompanyBpmnDsl
       |trait CompanyBpmnMessageEventDsl extends BpmnMessageEventDsl, CompanyBpmnDsl
       |trait CompanyBpmnSignalEventDsl extends BpmnSignalEventDsl, CompanyBpmnDsl
       |trait CompanyBpmnTimerEventDsl extends BpmnTimerEventDsl, CompanyBpmnDsl
       |""".stripMargin

  private lazy val apiWrapper =
    s"""package $companyName.camundala
       |package api
       |
       |/**
       | * Add here company specific stuff, to create the Api documentation and the Postman collection.
       | */
       |trait CompanyApiCreator extends ApiCreator, ApiDsl, CamundaPostmanApiCreator:
       |
       |  // override the config if needed
       |  protected def apiConfig: ApiConfig = CompanyApiCreator.apiConfig
       |
       |  lazy val companyProjectVersion = BuildInfo.version
       |
       |object CompanyApiCreator:
       |   lazy val apiConfig = ApiConfig(companyName = "$companyName")
       |""".stripMargin

  private lazy val dmnWrapper =
    s"""package $companyName.camundala.dmn
       |
       |trait CompanyDmnTester extends DmnTesterConfigCreator:
       |
       |  override def starterConfig: DmnTesterStarterConfig =
       |    DmnTesterStarterConfig(companyName = "$companyName")
       |
       |end CompanyDmnTester
       |""".stripMargin

  private lazy val simulationWrapper =
    s"""package $companyName.camundala.simulation
       |
       |import camundala.simulation.custom.*
       |
       |/**
       | * Add here company specific stuff, to run the Simulations.
       | */
       |trait CompanySimulation extends BasicSimulationDsl:
       |
       |  override def config =
       |    super.config //TODO Adjust config if needed
       |
       |end CompanySimulation
       |""".stripMargin

  private lazy val workerHandlerWrapper =
    s"""package $companyName.camundala.worker
       |
       |import camundala.camunda7.worker.C7WorkerHandler
       |import scala.reflect.ClassTag
       |
       |/**
       | * Add here company specific stuff, to run the Workers.
       | * You also define the implementation of the WorkerHandler here.
       | */
       |trait CompanyWorkerHandler extends C7WorkerHandler
       |
       |trait CompanyValidationWorkerDsl[
       |    In <: Product: InOutCodec
       |] extends CompanyWorkerHandler, ValidationWorkerDsl[In]
       |
       |trait CompanyInitWorkerDsl[
       |    In <: Product: InOutCodec,
       |    Out <: Product: InOutCodec,
       |    InitIn <: Product: InOutCodec,
       |    InConfig <: Product: InOutCodec
       |] extends CompanyWorkerHandler, InitWorkerDsl[In, Out, InitIn, InConfig]
       |
       |trait CompanyCustomWorkerDsl[
       |    In <: Product: InOutCodec,
       |    Out <: Product: InOutCodec
       |] extends CompanyWorkerHandler, CustomWorkerDsl[In, Out]
       |
       |trait CompanyServiceWorkerDsl[
       |    In <: Product: InOutCodec,
       |    Out <: Product: InOutCodec,
       |    ServiceIn: InOutEncoder,
       |    ServiceOut: InOutDecoder: ClassTag
       |] extends CompanyWorkerHandler, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]
       |""".stripMargin

  private lazy val workerContextWrapper =
    s"""package $companyName.camundala.worker
       |
       |import camundala.camunda7.worker.Camunda7Context
       |import scala.compiletime.uninitialized
       |import scala.reflect.ClassTag
       |
       |@SpringConfiguration
       |class CompanyEngineContext extends Camunda7Context:
       |
       |  @Autowired()
       |  var restApiClient: CompanyRestApiClient = uninitialized
       |
       |  override def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder: ClassTag](
       |      request: RunnableRequest[ServiceIn]
       |  ): SendRequestType[ServiceOut] =
       |    restApiClient.sendRequest(request)
       |
       |end CompanyEngineContext
       |""".stripMargin

  private lazy val workerPasswordWrapper =
    s"""package $companyName.camundala.worker
       |
       |import camundala.camunda7.worker.oauth.OAuthPasswordFlow
       |
       |trait CompanyPasswordFlow extends OAuthPasswordFlow:
       |
       |  lazy val fssoRealm: String = sys.env.getOrElse("FSSO_REALM", "myRealm")
       |  // default is a local keycloak server on colime docker environment
       |  lazy val fssoBaseUrl = sys.env.getOrElse("FSSO_BASE_URL", s"http://host.lima.internal:8090")
       |
       |  override lazy val client_id = sys.env.getOrElse("FSSO_CLIENT_NAME", "myClientKey")
       |  override lazy val client_secret = sys.env.getOrElse("FSSO_CLIENT_SECRET", "myClientSecret")
       |  override lazy val scope = sys.env.getOrElse("FSSO_SCOPE", "myScope")
       |  override lazy val username = sys.env.getOrElse("FSSO_TECHUSER_NAME", "myTechUser")
       |  override lazy val password = sys.env.getOrElse("FSSO_TECHUSER_PASSWORD", "myTechUserPassword")
       |
       |end CompanyPasswordFlow
       |""".stripMargin

  private lazy val workerRestApiWrapper =
    s"""package $companyName.camundala.worker
       |
       |import camundala.camunda7.worker.RestApiClient
       |import camundala.worker.CamundalaWorkerError.*
       |import sttp.client3.*
       |
       |@SpringConfiguration
       |class CompanyRestApiClient extends RestApiClient, CompanyPasswordFlow:
       |
       |  override protected def auth(
       |      request: Request[Either[String, String], Any]
       |  )(using
       |      context: EngineRunContext
       |  ): Either[ServiceAuthError, Request[Either[String, String], Any]] =
       |    tokenService.adminToken()
       |      .map:
       |        request.addToken
       |  end auth
       |
       |end CompanyRestApiClient
       |""".stripMargin

  private lazy val workerApplicationConfig =
    """server:
      |  port: 8093
      |camunda.bpm:
      |  job-execution:
      |    wait-time-in-millis: 200 # this is for speedup testing
      |  client:
      |    base-url: ${CAMUNDA_BASE_URL:http://localhost:8080/engine-rest}
      |    worker-id: ${WORKER_ID:my-worker}
      |    disable-backoff-strategy: true # only during testing - faster topic
      |    async-response-timeout: 10000
      |
      |spring:
      |  security:
      |    oauth2:
      |      client:
      |        registration:
      |          worker:
      |            provider: keycloak-primary
      |            client-id: myclient
      |            client-secret: ${FSSO_CLIENT_SECRET}
      |            clientName: ${FSSO_CLIENT_NAME:myclient}
      |            authorization-grant-type: client_credentials
      |            redirect-uri: '{baseUrl}/login/oauth2/code/{registrationId}'
      |            scope:
      |              - openid
      |              - profile
      |              - email
      |        provider:
      |          keycloak-primary:
      |            authorization-uri: ${FSSO_BASE_URL:http://kubernetes.docker.internal:8090/auth}/realms/${FSSO_REALM:master}/protocol/openid-connect/auth
      |            token-uri: ${FSSO_BASE_URL:http://kubernetes.docker.internal:8090/auth}/realms/${FSSO_REALM:master}/protocol/openid-connect/token
      |            user-info-uri: ${FSSO_BASE_URL:http://kubernetes.docker.internal:8090/auth}/realms/${FSSO_REALM:master}/protocol/openid-connect/userinfo
      |            jwk-set-uri: ${FSSO_BASE_URL:http://kubernetes.docker.internal:8090/auth}/realms/${FSSO_REALM:master}/protocol/openid-connect/certs
      |            user-name-attribute: preferred_username
      |
      |management:
      |  endpoints:
      |    web:
      |      exposure:
      |        include: "health"
      |""".stripMargin

  private lazy val helperCompanyDevHelperWrapper          =
    s"""package $companyName.camundala.helper
       |
       |import camundala.api.ApiConfig
       |import camundala.helper.dev.DevHelper
       |import camundala.helper.util.DevConfig
       |import $companyName.camundala.api.CompanyApiCreator
       |
       |case object CompanyDevHelper
       |    extends DevHelper:
       |
       |  lazy val apiConfig: ApiConfig = CompanyApiCreator.apiConfig
       |  lazy val devConfig: DevConfig = CompanyDevConfig.config
       |
       |end CompanyDevHelper
       |""".stripMargin
  end helperCompanyDevHelperWrapper
  private lazy val helperCompanyDevConfigWrapper          =
    s"""package $companyName.camundala.helper
       |
       |import camundala.api.*
       |import camundala.helper.util.*
       |import $companyName.camundala.BuildInfo
       |
       |object CompanyDevConfig:
       |
       |  lazy val companyConfig =
       |    DevConfig(
       |      ApiProjectConfig(
       |        projectName = BuildInfo.name,
       |        projectVersion = BuildInfo.version
       |      )
       |    )
       |
       |  lazy val config: DevConfig =
       |     config(ApiProjectConfig())
       |
       |  def config(apiProjectConfig: ApiProjectConfig) = DevConfig(
       |    apiProjectConfig,
       |    //sbtConfig = companySbtConfig,
       |    //versionConfig = companyVersionConfig,
       |    //publishConfig = Some(companyPublishConfig),
       |    //postmanConfig = Some(companyPostmanConfig),
       |    //dockerConfig = companyDockerConfig
       |  )
       |
       |  private lazy val companyVersionConfig = CompanyVersionConfig(
       |    scalaVersion = BuildInfo.scalaVersion,
       |    camundalaVersion = BuildInfo.camundalaV,
       |    companyCamundalaVersion = BuildInfo.version,
       |    sbtVersion = BuildInfo.sbtVersion,
       |    otherVersions = Map()
       |  )
       |end CompanyDevConfig
       |""".stripMargin
  end helperCompanyDevConfigWrapper
  private lazy val helperCompanyCamundalaDevHelperWrapper =
    s"""package $companyName.camundala.helper
       |
       |import camundala.api.ApiConfig
       |import camundala.helper.dev.DevCompanyCamundalaHelper
       |import camundala.helper.util.DevConfig
       |import $companyName.camundala.BuildInfo
       |import $companyName.camundala.api.CompanyApiCreator
       |
       |object CompanyCamundalaDevHelper
       |    extends DevCompanyCamundalaHelper:
       |
       |  lazy val apiConfig: ApiConfig = CompanyApiCreator.apiConfig
       |    .copy(
       |      basePath = os.pwd / "00-docs",
       |      tempGitDir = os.pwd / os.up / "git-temp"
       |    )
       |
       |  lazy val devConfig: DevConfig = CompanyDevConfig.companyConfig
       |
       |end CompanyCamundalaDevHelper
       |""".stripMargin
  end helperCompanyCamundalaDevHelperWrapper

  extension (module: ModuleConfig)
    def srcPath: os.Path      =
      config.projectDir / module.packagePath(
        config.projectPath
      )
    def resourcePath: os.Path =
      config.projectDir / module.packagePath(
        config.projectPath,
        isSourceDir = false
      )
  end extension
end CompanyWrapperGenerator
