package camundala.examples.invoice.bpmn

import camundala.domain.*
import camundala.examples.invoice.bpmn.InvoiceApi.*
import camundala.test.*
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import java.util
import java.util.{HashSet, List, Set}
import scala.compiletime.{constValue, constValueTuple}
import scala.deriving.Mirror

class ExampleInvoiceTest extends TestRunner:

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
    test(InvoiceReceiptP)(
      invoiceAssignApproverDMN2,
      // checkGroupIds,
      approveInvoiceUT,
      prepareBankTransferUT,
      archiveInvoiceST,
      InvoiceProcessedEE
    )
  import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
/*
  @Test
  def testInvoiceReceiptWithReview(): Unit =
    processEngineRule.manageDeployment(registerCallActivityMock(ReviewInvoiceP.id)
      .onExecutionAddVariables(new VariableMapImpl(ReviewInvoiceP.out.asJavaVars()))
      .deploy(processEngine()));
    test(
      invoiceReceiptProcess
        .withOut(InvoiceReceiptCheck(false))
    )(
      approveInvoiceUT
        .withOut(ApproveInvoice(false)),
      reviewInvoiceCA,
      approveInvoiceUT, // now we approve it
      prepareBankTransferUT
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
