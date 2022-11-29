package camundala.simulation.custom

import camundala.simulation.*
import io.circe.*
import sttp.client3.{Empty, RequestT}
import sttp.model.StatusCode

import scala.concurrent.duration.*
import scala.language.dynamics

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
