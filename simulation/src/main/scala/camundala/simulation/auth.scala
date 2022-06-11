package camundala.simulation

import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

trait BasicSimulationDsl extends SimulationDsl:
  def username = "demo"
  def password = "demo"

  override implicit def config: SimulationConfig =
    super.config
      .withAuthHeader((b: HttpRequestBuilder) =>
        b.basicAuth(username, password)
      )

case class Fsso(url: String, bodyForm: Map[String, String])

trait OAuthSimulationDsl extends SimulationDsl:

  def fsso: Fsso

  override implicit def config: SimulationConfig =
    super.config
      .withAuthHeader((b: HttpRequestBuilder) =>
        b.header("Authorization", s"Bearer #{access_token}")
      )
      .withPreRequest(getToken)

  private lazy val getToken: () => ChainBuilder = () =>
    exec(
      http("Get Access Token")
        .post(s"${fsso.url}/token")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .form(fsso.bodyForm)
        .check(extractJson("$.access_token", "access_token"))
    )
