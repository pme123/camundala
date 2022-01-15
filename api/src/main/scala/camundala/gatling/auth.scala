package camundala
package gatling

import io.gatling.core.Predef.*
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

case class Fsso(url: String, bodyForm: Map[String, String])

trait BasicSimulationRunner extends SimulationRunner:
  def username = "demo"
  def password = "demo"

  override def authHeader: HttpRequestBuilder => HttpRequestBuilder =
    _.basicAuth(username, password)

trait OAuthSimulationRunner extends SimulationRunner:

  def fsso: Fsso

  override def authHeader: HttpRequestBuilder => HttpRequestBuilder =
    _.header("Authorization", s"Bearer $${access_token}")

  override def preRequests: Seq[ChainBuilder] = Seq(getToken)

  private lazy val getToken: ChainBuilder =
    exec(
      http("Get Access Token")
        .post(s"${fsso.url}/token")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .form(fsso.bodyForm)
        .check(extractJson("$.access_token", "access_token"))
    )
