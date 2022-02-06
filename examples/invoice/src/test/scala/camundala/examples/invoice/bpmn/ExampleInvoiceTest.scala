package camundala.examples.invoice.bpmn

import camundala.domain.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
import camundala.test.*
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.generic.auto.*
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*

import java.util
import java.util.{HashSet, List, Set}
import scala.compiletime.{constValue, constValueTuple}
import scala.deriving.Mirror

class ExampleInvoiceTest extends ScenarioRunner:

  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / "invoice.v2.bpmn",
        baseResource / "reviewInvoice.bpmn",
        baseResource / "invoiceBusinessDecisions.dmn",
        formResource / "approve-invoice.html",
        formResource / "assign-reviewer.html",
        formResource / "prepare-bank-transfer.html",
        formResource / "review-invoice.html",
        formResource / "start-form.html"
      )
      .registries()

  @Test
  def testReviewReview(): Unit =
    test(ReviewInvoiceP)(
      assignReviewerUT,
      reviewInvoiceUT
    )

  @Test
  def testInvoiceReceipt(): Unit =
    println(s"InvoiceReceiptP: ${invoiceAssignApproverDMN2.out.asJson}")
    test(InvoiceReceiptP)(
      invoiceAssignApproverDMN2,
      checkGroupIds,
      approveInvoiceUT,
      prepareBankTransferUT,
      archiveInvoiceST,
      InvoiceProcessedEE
    )

    /*TODO see https://forum.camunda.org/t/mocking-call-activities-for-the-camunda-scenario-tests/27161/2
  import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine
  import org.camunda.bpm.engine.variable.impl.VariableMapImpl
  import org.camunda.bpm.extension.mockito.CamundaMockito.registerCallActivityMock

  @Test
  def testInvoiceReceiptWithReview(): Unit =
    processEngineRule.manageDeployment(registerCallActivityMock(ReviewInvoiceP.id)
      .onExecutionAddVariables(new VariableMapImpl(ReviewInvoiceP.out.asJavaVars()))
      .deploy(processEngine()));
    test(
      InvoiceReceiptP
        .withOut(InvoiceReceiptCheck(false))
    )(
      approveInvoiceUT
        .withOut(ApproveInvoice(false)),
      reviewInvoiceCA
        .withOut(InvoiceReviewed(false)),
    //  approveInvoiceUT, // now we approve it
    //  prepareBankTransferUT
    )
*/

  import scala.jdk.CollectionConverters.IterableHasAsScala

  private def checkGroupIds =
    custom {
      val links = taskService.getIdentityLinksForTask(task.getId).asScala
      val approverGroups = new util.HashSet[String]
      for (link <- links) {
        approverGroups.add(link.getGroupId)
      }
      assertEquals(2, approverGroups.size)
      assertTrue(approverGroups.contains("accounting"))
      assertTrue(approverGroups.contains("sales"))
    }
