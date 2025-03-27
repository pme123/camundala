package camundala.worker
package oauth

import camundala.worker.CamundalaWorkerError.ServiceAuthError
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.Uri

class TokenService(
    identityUrl: Uri,
    adminTokenBody: Map[String, String],
    clientCredentialsBody: Map[String, String],
    impersonateBody: Map[String, String]
):

  def adminToken(): Either[ServiceAuthError, String] =
    authAdminResponse
      .body
      .map(t => s"Bearer ${t.access_token}")
      .left
      .map(err =>
        ServiceAuthError(
          s"Could not get a token for '${adminTokenBody("username")}'!\n$err\n\n$identityUrl"
        )
      )
  def clientCredentialsToken(): Either[ServiceAuthError, String] =
    authClientCredentialsResponse
      .body
      .map(t => s"Bearer ${t.access_token}")
      .left
      .map(err =>
        ServiceAuthError(
          s"Could not get a token for '${clientCredentialsBody("client_id")}' -> ClientCredentials!\n$err\n\n$identityUrl"
        )
      )

  def impersonateToken(username: String, adminToken: String): IO[ServiceAuthError, String] =
    val token = adminToken.replace("Bearer ", "")
    val body  = impersonateBody ++ Map("requested_subject" -> username, "subject_token" -> token)
    ZIO.fromEither(authImpersonateResponse(body)
      .body
      .map(t => s"Bearer ${t.access_token}")).mapError(err =>
      ServiceAuthError(
        s"Could not get impersonated token for $username - ${token.take(20)}...${token.takeRight(10)}!\n$err\n\n$identityUrl"
      )
    )
  end impersonateToken
  private lazy val tokenRequest                                                            =
    basicRequest
      .post(identityUrl)
      .header("accept", "application/json")

  private def authAdminResponse                                  =
    tokenRequest.body(adminTokenBody).response(asJson[TokenResponse]).send(backend)
  private def authClientCredentialsResponse                      =
    tokenRequest.body(clientCredentialsBody).response(asJson[TokenResponse]).send(backend)
  private def authImpersonateResponse(body: Map[String, String]) =
    tokenRequest.body(body).response(asJson[TokenResponse]).send(backend)
end TokenService
