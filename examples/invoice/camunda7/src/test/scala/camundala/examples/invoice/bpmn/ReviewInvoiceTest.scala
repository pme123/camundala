package camundala.examples.invoice.bpmn

import camundala.bpmn.*

import camundala.test.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import sttp.tapir.generic.auto.*

import java.util
import java.util.{HashSet, List, Set}
import scala.compiletime.{constValue, constValueTuple}
import scala.deriving.Mirror

class ReviewInvoiceTest extends ScenarioRunner:

  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / "reviewInvoice.bpmn",
        formResource / "assign-reviewer.html",
        formResource / "review-invoice.html",
      )
      .registries()

  @Test
  def testReviewReview(): Unit =
    test(`Review Invoice`)(
      AssignReviewerUT,
      ReviewInvoiceUT
    )
