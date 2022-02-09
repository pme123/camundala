package camundala.test

import camundala.bpmn.*
import io.circe.generic.auto.*
import org.junit.Test
import os.{Path, ResourcePath}
import sttp.tapir.generic.auto.*

import java.time.{LocalDate, LocalDateTime, ZoneId, ZonedDateTime}

class DecisionTypesTest extends DmnTestRunner, BpmnDsl:

  val dmnPath: ResourcePath = baseResource / "DecisionTypes.dmn"

  case class Input(dateTest: LocalDateTime)
  case class Output(dateOut: LocalDateTime)


  private val ldtIn: LocalDateTime = LocalDateTime.of(2021, 12, 24, 0, 0)
  private val ldtOut: LocalDateTime = LocalDateTime.of(2021, 12, 12, 0, 0)
  private lazy val localDateTimeDMN = singleEntry(
    decisionDefinitionKey = "DecisionTypes",
    in = Input(ldtIn),
    out = ldtOut
  )

  case class ZInput(dateTest: ZonedDateTime)

  private lazy val zonedDateTimeDMN = singleEntry(
    decisionDefinitionKey = "DecisionTypes",
    in = ZInput(ZonedDateTime.of(ldtIn, ZoneId.systemDefault)),
    out = ZonedDateTime.of(ldtOut, ZoneId.systemDefault)
  )

  @Test
  def testLocalDateTime(): Unit =
    test(localDateTimeDMN)

  @Test
  def testZonedDateTime(): Unit =
    test(zonedDateTimeDMN)