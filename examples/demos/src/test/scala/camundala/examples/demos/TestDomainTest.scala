package camundala.examples.demos


import camundala.test.*
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.{Ignore, Test}
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import io.circe.syntax.*
import java.util
import java.util.{HashSet, List, Set}
import scala.compiletime.{constValue, constValueTuple}
import scala.deriving.Mirror

class TestDomainTest extends TestRunner:

  import org.camunda.spin.Spin.*

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