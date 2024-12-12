package camundala.helper.dev.company

import camundala.helper.dev.update.createIfNotExists
import camundala.helper.util.*

case class CompanyWrapperGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createIfNotExists(projectBpmnPath, bpmnWrapper)
    createIfNotExists(projectApiPath, apiWrapper)
    createIfNotExists(projectDmnPath, dmnWrapper)
    createIfNotExists(projectSimulationPath, simulationWrapper)
    createIfNotExists(projectWorkerPath, workerWrapper)
    createIfNotExists(projectHelperPath, helperWrapper)

  private lazy val companyName = config.companyName

  private lazy val projectBpmnPath = ModuleConfig.bpmnModule.srcPath / "CompanyBpmnDsl.scala"
  private lazy val projectApiPath = ModuleConfig.apiModule.srcPath / "CompanyApiCreator.scala"
  private lazy val projectDmnPath = ModuleConfig.dmnModule.srcPath / "CompanyDmnTester.scala"
  private lazy val projectSimulationPath = ModuleConfig.simulationModule.srcPath / "CompanySimulation.scala"
  private lazy val projectWorkerPath = ModuleConfig.workerModule.srcPath / "CompanyWorkerHandler.scala"
  private lazy val projectHelperPath = ModuleConfig.helperModule.srcPath / "CompanyDevHelper.scala"

  private lazy val bpmnWrapper =
    s"""package $companyName.camundala.bpmn
       |
       |import camundala.bpmn.*
       |import camundala.domain.*
       |
       |/**
       | * Add here company specific stuff, like documentation or custom elements.
       | */
       |trait CompanyBpmnDsl
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
    s"""package $companyName.camundala.api
       |
       |import camundala.api.*
       |
       |/**
       | * Add here company specific stuff, to create the Api documentation and the Postman collection.
       | */
       |trait CompanyApiCreator extends ApiCreator, ApiDsl, CamundaPostmanApiCreator:
       |
       |  // override the config if needed
       |  protected def apiConfig: ApiConfig = CompanyApiCreator.apiConfig
       |
       |  lazy val companyDescr = ??? //TODO Add your Company Description!
       |
       |object CompanyApiCreator:
       |   lazy val apiConfig = ApiConfig(companyId = "$companyName")
       |""".stripMargin

  private lazy val dmnWrapper =
    s"""package $companyName.camundala.dmn
       |
       |import camundala.dmn.*
       |
       |/**
       | * Add here company specific stuff, to run the DMN Tester.
       | */
       |trait CompanyDmnTester extends DmnTesterConfigCreator, DmnTesterStarter:
       |
       |  def starterConfig: DmnTesterStarterConfig =
       |    DmnTesterStarterConfig( // adjust paths if needed
       |      companyName = "$companyName",
       |    )
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
       |  override implicit def config =
       |    super.config //TODO Adjust config if needed
       |""".stripMargin

  private lazy val workerWrapper =
    s"""package $companyName.camundala.worker
       |
       |import camundala.camunda7.worker.C7WorkerHandler
       |import camundala.worker.*
       |
       |import scala.reflect.ClassTag
       |
       |/**
       | * Add here company specific stuff, to run the Workers.
       | * You also define the implementation of the WorkerHandler here.
       | */
       |trait CompanyWorkerHandler extends C7WorkerHandler
       |
       |trait CompanyInitWorkerDsl[
       |    In <: Product: InOutCodec,
       |    Out <: Product: InOutCodec,
       |    InitIn <: Product: InOutCodec,
       |    InConfig <: Product: InOutCodec
       |] extends CompanyWorkerHandler, InitWorkerDsl[In, Out, InitIn, InConfig]
       |
       |trait CompanyValidationWorkerDsl[
       |    In <: Product: InOutCodec
       |] extends CompanyWorkerHandler, ValidationWorkerDsl[In]
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

  private lazy val helperWrapper =
    s"""package $companyName.camundala.helper
       |
       |import camundala.api.ApiConfig
       |import camundala.helper.dev.*
       |import camundala.helper.util.*
       |import mycompany.camundala.api.CompanyApiCreator
       |
       |case class CompanyDevHelper(projectName: String, subProjects: Seq[String] = Seq.empty) extends DevHelper:
       |
       |  lazy val apiConfig: ApiConfig = CompanyApiCreator.apiConfig
       |
       |  lazy val devConfig: DevConfig =
       |    DevConfig.defaultConfig(projectName) //TODO Implement your Config!
       |      .copy(subProjects = subProjects)
       |
       |  lazy val publishConfig: Option[PublishConfig] = None //TODO If you have a webdav server to publish the docs, add the config here
       |  lazy val deployConfig: Option[DeployConfig] = None //TODO If you have a Postman account, add the config here
       |  lazy val dockerConfig: DockerConfig = DockerConfig() //TODO Adjust the DockerConfig if needed
       |""".stripMargin
  end helperWrapper

  extension (module: ModuleConfig)
    def srcPath: os.Path =
      config.projectDir / module.packagePath(
        config.projectPath
      )
end CompanyWrapperGenerator
