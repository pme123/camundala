package camundala.examples.demos

import camundala.domain.*
import camundala.test.*
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test

import java.util
import java.util.{HashSet, List, Set}
import scala.compiletime.{constValue, constValueTuple}
import scala.deriving.Mirror

class TestDomainTest extends TestRunner:

  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / "generate-test.bpmn",
      )
  import TestDomain.*

  @Test
  def testReviewReview(): Unit =
    test(CamundalaGenerateTestP)(
    )