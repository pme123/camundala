package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import pme123.camundala.camunda.{CamundaLayers, camundaProcessEngine, httpDeployClient}
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.deploy.{Deploy, DockerConfig}
import pme123.camundala.model.register.bpmnRegister
import zio.Task
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, test, _}

import scala.jdk.CollectionConverters._

object ChangeAddressBpmnSuite extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("BpmnSuite")(
      suite("Generate BPMN")(
        test("Change Address Bpmn") {
          val bpmn = ChangeAddressBpmn().ChangeAddressBpmn
          val bpmnStr = bpmn.generateDsl()
          println(s"BPMN: $bpmnStr")
          assert(bpmnStr)(containsString("lazy val ChangeAddressBpmn: Bpmn =")) &&
            assert(bpmnStr)(containsString("""("ChangeAddress.bpmn",""")) &&
            assert(bpmnStr)(containsString("""BpmnProcess("ChangeAddressDemo")"""))
        }),
      suite("general tests")(
        testM("run happy path") {
          val changeAddressBpmn = ChangeAddressBpmn(kubeGroup, complianceGroup).ChangeAddressBpmn
          lazy val devDeploy =
            Deploy()
              .---(changeAddressBpmn)
              .---(DockerConfig.DefaultDevConfig.dockerDir("examples/docker"))
              .addUsers(heidi, kermit, adminUser)

          for {
            _ <- bpmnRegister.registerBpmn(changeAddressBpmn)
            _ <- httpDeployClient.deploy(devDeploy)
            runtimeService <- camundaProcessEngine.runtimeService
            processInstance <- Task(runtimeService.startProcessInstanceByKey(changeAddressBpmn.processes.head.id.value,
                Map[String, AnyRef]("customer" -> "meier").asJava))
          } yield assert(processInstance.getBusinessKey)(equalTo(null))
        }
      ).provideCustomLayer(ModelLayers.bpmnRegisterLayer ++
        CamundaLayers.camundaProcessEngineLayer ++
        CamundaLayers.httpDeployClientLayer).mapError(TestFailure.fail)
    )

}
