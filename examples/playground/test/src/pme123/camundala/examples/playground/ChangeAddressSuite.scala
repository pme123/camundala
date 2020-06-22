package pme123.camundala.examples.playground

import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{BeforeClass, Ignore, Rule, Test}
import pme123.camundala.camunda.{CamundaLayers, bpmnService}
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.register.bpmnRegister
import UsersAndGroups._
import org.camunda.bpm.engine.test.assertions.bpmn.TaskAssert

import scala.annotation.meta.getter
import zio._
import zio.Runtime.default.unsafeRun

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
    (bpmnRegister.registerBpmn(bpmn.ChangeAddressBpmn) *>
      bpmnService.generateBpmn(bpmn.ChangeAddressBpmn.id))
      .mapError(e =>
        logging.log.error("Problem generating BPMN files" + e.toString))
      .map(paths => logging.log.info(s"Paths generated: $paths"))
      .provideCustomLayer(
        ModelLayers.bpmnRegisterLayer ++
          CamundaLayers.bpmnServiceLayer ++
          CamundaLayers.logLayer("ChangeAddressSuite")
      ).unit
  }

  @(Rule@getter)
  val processEngineRule: ProcessEngineRule = new ProcessEngineRule()

  @Test
  @Deployment(resources = Array(
    "_generated/bpmn/ChangeAddress.bpmn",
    "_generated/bpmn/scripts/dmn-in-existing-country.groovy",
    "_generated/bpmn/scripts/dmn-in-new-country.groovy",
    "_generated/bpmn/scripts/form-json.groovy",
    "_generated/bpmn/country-risk.dmn"))
  def testHappyPath(): Unit = {

    val process = bpmn.ChangeAddressDemo

    val processInstance = runtimeService.startProcessInstanceByKey(process.id.value,  Map[String, AnyRef]("customer" -> "meier").asJava)

    assertThat(processInstance)
      .isStarted
      .hasVariables("customer")
      .isWaitingAt("GetAddressTask")
      .task()
     /* .hasName(bpmn.AddressChangeTask.id.value)
      .hasCandidateGroup(UsersAndGroups.kubeGroup.id.value)
      .isNotAssigned*/
    () // returns Unit
  }
}
