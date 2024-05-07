package camundala.bpmn

import munit.FunSuite

import java.time.LocalDateTime

class LocalDateTimeDecoderTest extends FunSuite:

  test("Date format LocalDateTime Format"):
    val date = "2024-04-23T14:54:19".asJson.as[LocalDateTime]
    assertEquals(date.toOption.get.toString, "2024-04-23T14:54:19")

  test("Date format LocalDateTime Format with milliseconds"):
    val date = "2024-04-23T14:54:19.505854264".asJson.as[LocalDateTime]
    assertEquals(date.toOption.get.toString, "2024-04-23T14:54:19.505854264")

  test("Date format ISO Format"):
    val date = "2024-04-23T14:54:19.505854264Z".asJson.as[LocalDateTime]
    assert(date.toOption.get.toString.endsWith(":54:19.505854264")) // problem with other system time on CI

  test("Date format bad"):
    val date = "2024-04-23T14:54:19.xyz".asJson.as[LocalDateTime]
    assertEquals(date.left.toOption.get.toString, "DecodingFailure at : Could not parse LocalDateTime from 2024-04-23T14:54:19.xyz")