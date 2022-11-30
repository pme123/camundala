package camundala.simulation.custom

import camundala.simulation.*
import io.circe.*
import sttp.client3.{Empty, RequestT}
import sttp.model.StatusCode

import scala.concurrent.duration.*
import scala.language.dynamics
import scala.util.Try

trait SimulationHelper extends ResultChecker, Logging:

  implicit def config: SimulationConfig[RequestT[Empty, Either[String, String], Any]] = SimulationConfig[RequestT[Empty, Either[String, String], Any]]()

  extension (request: RequestT[Empty, Either[String, String], Any])
    def auth(): RequestT[Empty, Either[String, String], Any] =
      config.authHeader(request)


  protected def handleNon2xxResponse(httpStatus: StatusCode, body: Object, curl: String)(
    using data:ScenarioData): ScenarioData =
      data
        .error(
          s"Non-2xx response to GET with code $httpStatus:\n$body"
        )
        .info(curl)

  protected def tryOrFail(funct: ScenarioData => ResultType, step: SStep)(using data: ScenarioData) = {
    val count = summon[ScenarioData].context.requestCount
    if (count < config.maxCount) {
      Try(Thread.sleep(1000)).toEither.left
        .map(_ =>
          summon[ScenarioData]
            .error(s"Interrupted Exception when waiting for ${step.name} (${step.typeName}).")
        )
        .flatMap { _ =>
          funct(
            summon[ScenarioData]
              .withRequestCount(count + 1)
              .info(s"Waiting for ${step.name} (${step.typeName} - count: $count)")
          )
        }
    } else {
      Left(
        summon[ScenarioData]
          .error(s"Expected ${step.name} (${step.typeName}) was not found! Tried $count times.")
      )
    }
  }