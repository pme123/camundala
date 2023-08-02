package camundala.simulation.custom

import camundala.simulation.*
import io.circe.*
import sttp.client3.{
  Empty,
  HttpClientSyncBackend,
  Identity,
  Request,
  RequestT,
  SttpBackend
}
import sttp.model.StatusCode

import scala.language.dynamics
import scala.util.Try

trait SimulationHelper extends ResultChecker, Logging:

  implicit def config
      : SimulationConfig[RequestT[Empty, Either[String, String], Any]] =
    SimulationConfig[RequestT[Empty, Either[String, String], Any]]()

  lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
  lazy val cockpitUrl =
    config.endpoint.replace("/engine-rest", "/camunda/app/cockpit/default")
  extension (request: RequestT[Empty, Either[String, String], Any])
    def auth(): RequestT[Empty, Either[String, String], Any] =
      config.authHeader(request)

  extension (request: Request[Either[String, String], Any])
    def extractBody()(using ScenarioData): Either[ScenarioData, Json] =
      val response = request.send(backend)
      response.body.left
        .map(body => handleNon2xxResponse(response.code, body, request.toCurl))
        .flatMap(
          parser
            .parse(_)
            .left
            .map(err =>
              summon[ScenarioData]
                .error(s"Problem creating body from response.")
                .info(err.toString)
            )
        )

  protected def handleNon2xxResponse(
      httpStatus: StatusCode,
      body: Object,
      curl: String
  )(using data: ScenarioData): ScenarioData =
    data
      .error(
        s"Non-2xx response to GET with code $httpStatus:\n$body"
      )
      .info(curl)

  protected def runRequest(
      request: Request[Either[String, String], Any],
      debugMsg: String
  )(
      handleBody: (Json, ScenarioData) => ResultType
  )(using data: ScenarioData): ResultType =
    given ScenarioData = data
      .info(debugMsg)
      .debug(s"- URI: ${request.uri}")
      .debug(s"- Body: ${request.body}")

    val response = request.send(backend)
    if (StatusCode.NoContent == response.code)
      handleBody(Json.Null, summon[ScenarioData])
    else
      response.body.left
        .map(body => handleNon2xxResponse(response.code, body, request.toCurl))
        .flatMap(
          parser.parse(_).left
            .map(err =>
              summon[ScenarioData]
                .error(s"Problem creating body from response.\n$err")
            )
        )
        .flatMap(handleBody(_, summon[ScenarioData]))

  protected def tryOrFail(
      funct: ScenarioData => ResultType,
      step: ScenarioOrStep
  )(using data: ScenarioData): ResultType = {
    val count = summon[ScenarioData].context.requestCount
    if (count < config.maxCount) {
      Try(Thread.sleep(1000)).toEither.left
        .map(_ =>
          summon[ScenarioData]
            .error(
              s"Interrupted Exception when waiting for ${step.name} (${step.typeName})."
            )
        )
        .flatMap { _ =>
          funct(
            summon[ScenarioData]
              .withRequestCount(count + 1)
              .info(
                s"Waiting for ${step.name} (${step.typeName} - count: $count)"
              )
          )
        }
    } else {
      Left(
        summon[ScenarioData]
          .error(
            s"Expected ${step.name} (${step.typeName}) was not found! Tried $count times."
          )
      )
    }
  }

  protected def waitFor(seconds: Int)(using data: ScenarioData): Either[ScenarioData, ScenarioData] =
    Try(Thread.sleep(seconds * 1000)).toEither
      .map(_ => data.info(s"Waited for $seconds second(s)."))
      .left
      .map(ex =>
        data
          .error(
            s"Problem when waiting for $seconds second(s). ${ex.getMessage}."
          )
          .debug(ex.toString)
      )
  end waitFor

end SimulationHelper