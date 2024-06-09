package camundala.simulation
package custom

import sttp.client3.*

trait BasicSimulationDsl extends CustomSimulation:
  def username = "demo"
  def password = "demo"
  override implicit def config: SimulationConfig[RequestT[Empty, Either[String, String], Any]] =
    super.config
      .withAuthHeader((r: RequestT[Empty, Either[String, String], Any]) =>
        r.auth.basic(username, password)
      )
end BasicSimulationDsl

object BasicSimulationDsl

case class Fsso(url: String, bodyForm: Map[String, String])

trait OAuthSimulationDsl extends CustomSimulation:

  def fsso: Fsso

  override implicit def config: SimulationConfig[RequestT[Empty, Either[String, String], Any]] =
    super.config
      .withAuthHeader((r: RequestT[Empty, Either[String, String], Any]) =>
        val token = getToken
        r.header("Authorization", s"Bearer $token")
      )

  private lazy val getToken: String =
    val uri = uri"${fsso.url}/token"
    val request = basicRequest
      .header("Content-Type", "application/x-www-form-urlencoded")
      .body(fsso.bodyForm)
      .post(uri)
    request
      .send(backend)
      .body
      .flatMap(parser.parse)
      .flatMap(body =>
        body.hcursor
          .downField("access_token")
          .as[String]
      ) match
      case Right(token) => token
      case Left(err) =>
        throw new IllegalArgumentException(s"Could not get a token!\n$err")
    end match
  end getToken
end OAuthSimulationDsl
