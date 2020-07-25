package pme123.camundala.examples.playground

import org.assertj.core.api.Assertions.{assertThat => jassertThat}
import org.camunda.bpm.engine.test._
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{assertThat, processInstanceQuery, runtimeService, task, taskService, _}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit._
import pme123.camundala.camunda.{CamundaLayers, bpmnService}
import pme123.camundala.examples.playground.UsersAndGroups._
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.register.bpmnRegister
import zio.Runtime.default.unsafeRun
import zio._

import scala.annotation.meta.getter
import scala.jdk.CollectionConverters._

/**
  * BE AWARE: if you have changes - the generated BPMNs are not available right away -
  * RUN THE TEST TWICE!
  *
  * // DOES NOT WORK - see https://forum.camunda.org/t/spin-is-not-available-in-my-camunda-assert-test/20810
  */
@RunWith(classOf[JUnit4])
class ChangeAddressSuite {

  private val bpmn: ChangeAddressBpmn = ChangeAddressBpmn(kubeGroup, complianceGroup)

  @BeforeClass
  val init: Unit = unsafeRun {
    (bpmnRegister.registerBpmn(bpmn.ChangeAddressTestBpmn) *>
      bpmnService.generateBpmn(bpmn.ChangeAddressTestBpmn.id))
      .bimap(
        e => logging.log.error("Problem generating BPMN files" + e.toString),
        paths => logging.log.info(s"Paths generated: $paths")
      )
      .provideCustomLayer(
        ModelLayers.bpmnRegisterLayer ++
          CamundaLayers.bpmnServiceLayer ++
          CamundaLayers.logLayer("ChangeAddressSuite")
      ).unit
  }

/*
  @Before
  def initTest: Unit = {
    //    Mocks.register("restService", new RestServiceDelegate())
  }*/

  import org.camunda.bpm.engine.test.mock.Mocks
  import org.junit.After

  @After def tearDown(): Unit = {
    Mocks.reset()
  }

  @(Rule@getter)
  val processEngineRule: ProcessEngineRule = new ProcessEngineRule()

  @Test
  @Ignore
  @Deployment(resources = Array(
    "_generated/bpmn/ChangeAddress.bpmn",
    "_generated/bpmn/scripts/dmn-in-existing-country.groovy",
    "_generated/bpmn/scripts/dmn-in-new-country.groovy",
    "_generated/bpmn/scripts/form-json.groovy",
    "_generated/bpmn/country-risk.dmn"))
  def testHappyPath(): Unit = {

    val process = bpmn.ChangeAddressDemo

    val processInstance = runtimeService.startProcessInstanceByKey(process.id.value,
      Map[String, AnyRef]("customer" -> "meier").asJava)

   // val taskService = processEngineRule.getTaskService

    assertThat(processInstance)
      .isStarted
      .hasVariables("customer")
      .isWaitingAt("GetAddressTask")

    jassertThat(processInstanceQuery.count()).isEqualTo(1L)

    //  assertThat(job).hasActivityId("GetAddressTask")
    // println("JOB: " + job().getJobDefinitionId)

    //    println("JOB: " + job())
    assertThat(task()).isNotNull
    assertThat(processInstance)
      .hasVariables("existingAddress")

    println("TASK: " + task())
    /*  assertThat(task(processInstance)).isNotNull
      claim(task("AddressChangeTask"), "kermit")
      assertThat(processInstance)
        .isWaitingAt("AddressChangeTask")
       .hasName(bpmn.AddressChangeTask.id.value)
       .hasCandidateGroup(UsersAndGroups.kubeGroup.id.value)
       .isNotAssigned*/
  }

  import org.camunda.bpm.model.bpmn.{Bpmn, GatewayDirection}

  @Test
  @Deployment(resources = Array(
    "_generated/bpmn/ChangeAddressTest.bpmn"
  ))
  def testHappyPathTest(): Unit = {

    val process = bpmn.ChangeAddressTestBpmn.processes.head

    val processInstance = runtimeService.startProcessInstanceByKey(process.id.value,
      Map[String, AnyRef]("customer" -> "meier").asJava)

    assertThat(processInstance)
      .isStarted
      .hasVariables("customer")
      .isWaitingAt("CustomerEditTask")

    // CustomerEditTask
    val taskQuery = taskService.createTaskQuery
    jassertThat(taskQuery.count()).isEqualTo(1L)
    taskService.complete(taskQuery.singleResult.getId)
  //  jassertThat(runtimeService.createProcessInstanceQuery.count).isEqualTo(0)

    // AddressChangeTask
    val taskQuery2 = taskService.createTaskQuery
    jassertThat(taskQuery2.count()).isEqualTo(1L)
    taskService.complete(taskQuery2.singleResult.getId)
    jassertThat(runtimeService.createProcessInstanceQuery.count).isEqualTo(0)
    ()
  }

  @Test
  @Ignore
  def testCreateInvoiceProcess(): Unit = {
    val modelInstance = Bpmn
      .createExecutableProcess("invoice").name("BPMN API Invoice Process")
      .startEvent.name("Invoice received")
      .userTask.name("Assign Approver")
      .camundaAssignee("demo")
      .userTask.id("approveInvoice")
      .name("Approve Invoice")
      .exclusiveGateway.name("Invoice approved?")
      .gatewayDirection(GatewayDirection.Diverging)
      .condition("yes", "${approved}")
      .userTask.name("Prepare Bank Transfer")
      .camundaFormKey("embedded:app:forms/prepare-bank-transfer.html")
      .camundaCandidateGroups("accounting")
      .serviceTask.name("Archive Invoice")
      .camundaClass("pme123.camundala.examples.playground.ArchiveInvoiceService")
      .endEvent.name("Invoice processed")
      .moveToLastGateway.condition("no", s"$${!approved}")
      .userTask.name("Review Invoice")
      .camundaAssignee("demo")
      .exclusiveGateway.name("Review successful?")
      .gatewayDirection(GatewayDirection.Diverging).condition("no", s"$${!clarified}")
      .endEvent.name("Invoice not processed")
      .moveToLastGateway.condition("yes", "${clarified}")
      .connectTo("approveInvoice")
      .done

    // deploy process model
    repositoryService.createDeployment.addModelInstance("invoice.bpmn", modelInstance).deploy
    // start process model
    runtimeService.startProcessInstanceByKey("invoice")
    val taskQuery = taskService.createTaskQuery
    // check and complete task "Assign Approver"
    org.junit.Assert.assertEquals(1, taskQuery.count)
    taskService.complete(taskQuery.singleResult.getId)
    // check and complete task "Approve Invoice"
    //val variables: Map[String, Any] = Map("approved" -> true)
    org.junit.Assert.assertEquals(1, taskQuery.count)
    //taskService.complete(taskQuery.singleResult.getId, variables.asJava)
    // check and complete task "Prepare Bank Transfer"
    org.junit.Assert.assertEquals(1, taskQuery.count)
    taskService.complete(taskQuery.singleResult.getId)
    // check if Delegate was executed
    org.junit.Assert.assertEquals(true, ArchiveInvoiceService.wasExecuted)
    // check if process instance is ended
    org.junit.Assert.assertEquals(0, runtimeService.createProcessInstanceQuery.count)

    /**
      * to see the BPMN 2.0 process model XML on the console log
      * copy the following code line at the end of the test case
      *
      * Bpmn.writeModelToStream(System.out, modelInstance);
      */
  }


}

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}

object ArchiveInvoiceService {
  var wasExecuted = false
}

class ArchiveInvoiceService extends JavaDelegate {

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit = {
    ArchiveInvoiceService.wasExecuted = true
    println("\n\n  ... Now archiving invoice " + execution.getVariable("invoiceNumber") + " \n\n")
  }
}
