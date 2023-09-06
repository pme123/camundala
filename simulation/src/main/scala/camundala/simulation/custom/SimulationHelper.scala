package camundala.simulation.custom

import camundala.simulation.*
import io.circe.*
import sttp.client3.*
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
          parser
            .parse(_)
            .left
            .map(err =>
              summon[ScenarioData]
                .error(s"Problem creating body from response.\n$err")
            )
        )
        .flatMap(handleBody(_, summon[ScenarioData]))

  extension(step: ScenarioOrStep)

    protected def tryOrFail(
        funct: ScenarioData => ResultType,
    )(using data: ScenarioData): ResultType = {
      val count = summon[ScenarioData].context.requestCount
      if (count < config.maxCount) {
        Try(Thread.sleep(1000)).toEither.left
          .map(_ =>
            summon[ScenarioData]
              .error(
                s"Interrupted Exception when waiting for ${step.name} (${step.typeName})."
              )
          ).flatMap { _ =>
            if (!step.isInstanceOf[IsIncidentScenario])
              checkIfIncidentOccurred(data)
            else
              Right(data)
          }
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

    protected def waitFor(
        seconds: Int
    )(using data: ScenarioData): Either[ScenarioData, ScenarioData] =
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

    protected def checkIfIncidentOccurred(data: ScenarioData): ResultType =
      handleIncident()(data) { (body, data) =>
        body.hcursor.values
          .map {
            case (values: Iterable[Json]) if values.toSeq.nonEmpty =>
              extractIncidentMsg(body)(data)
                .flatMap { case (incidentMessage, _, _) =>
                  Left(
                    data.error(
                      s"There is a NON-EXPECTED error occurred: ${
                        incidentMessage
                          .getOrElse("No incident message")
                      }!"
                    )
                  )
                }
            case _ =>
              Right(
                data
                  .debug(
                    s"No incident so far for ${step.name}."
                  )
              )
          }
          .getOrElse(
            Left(
              data.error(
                "An Array is expected (should not happen)."
              )
            )
          )
      }

    protected def handleIncident(
                        rootIncidentId: Option[String] = None
                      )(data: ScenarioData)(
                        handleBody: (Json, ScenarioData) => ResultType
                      ): ResultType =
      val processInstanceId = data.context.processInstanceId
      val uri = rootIncidentId match
        case Some(incId) =>
          uri"${config.endpoint}/incident?incidentId=$incId&deserializeValues=false"
        case None =>
          uri"${config.endpoint}/incident?processInstanceId=$processInstanceId&deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)

      given ScenarioData = data

      runRequest(request, s"Process '${step.name}' checkIncident") {
        (body, data) => handleBody(body, data)
      }.left.map(_.info(request.toCurl))

    end handleIncident

    protected def extractIncidentMsg(body: Json)(
      data: ScenarioData
    ): Either[ScenarioData, (Option[String], String, String)] =
      val arr = body.hcursor.downArray
      (for
        maybeIncMessage <- arr
          .downField("incidentMessage")
          .as[Option[String]]
        id <- arr.downField("id").as[String]
        rootCauseIncidentId <- arr
          .downField("rootCauseIncidentId")
          .as[String]
      yield (maybeIncMessage, id, rootCauseIncidentId)).left
        .map { ex =>
          data
            .error(
              s"Problem extracting incidentMessage from $body\n $ex"
            )
        }
    end extractIncidentMsg
  end extension
end SimulationHelper
