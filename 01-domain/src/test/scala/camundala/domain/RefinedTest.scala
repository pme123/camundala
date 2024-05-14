package camundala.domain

import munit.FunSuite
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import sttp.tapir.codec.iron.given
import io.circe.syntax.*

class RefinedTest extends FunSuite:

  test("Percentage"):
    val p: Percentage = 80
    assertEquals(
      p.asJson, Json.fromInt(80)
    )
  test("Iso8601Day"):
    val d: Iso8601Day = "2023-01-04"
    assertEquals(
      d.asJson, Json.fromString("2023-01-04")
    )
  test("Iso8601DateTime"):
    val _: Iso8601DateTime = "2023-01-04T12:12:12Z"

  test("Iso8601Duration"):
    val _: Iso8601Duration = "PT3S"

  test("Iso8601Interval"):
    val _: Iso8601Interval = "R2/PT3S"

  test("CronExpr"):
    val _: CronExpr = "0 0 7,11,15 ? * MON-FRI"
  test("Iban"):
    val _: Iban = "CH12 3123 1436 6943 0200 4"
    val _: Iban = "CH1231231436694302004"
