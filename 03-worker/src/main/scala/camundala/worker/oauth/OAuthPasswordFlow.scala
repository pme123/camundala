package camundala.worker.oauth

import camundala.domain.InOutDecoder
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.ServiceAuthError
import sttp.client3.*

trait OAuthPasswordFlow:
  def fssoRealm: String
  def fssoBaseUrl: String
  def identityUrl =
    uri"$fssoBaseUrl/realms/$fssoRealm/protocol/openid-connect/token"

  def grant_type_password           = "password"
  def grant_type_client_credentials = "client_credentials"
  def grant_type_impersonate        = "urn:ietf:params:oauth:grant-type:token-exchange"
  def client_id                     = "myClientId"
  def client_secret                 = "myClientSecret"
  def scope                         = "openid"
  // Grant Type password
  def username                      = "admin"
  def password                      = "admin"

  def tokenService = TokenService(
    identityUrl,
    tokenRequestBody,
    clientCredentialsTokenRequestBody,
    impersonateTokenRequestBody
  )

  def adminToken(tokenKey: String = username)(using
      logger: WorkerLogger,
  ): Either[ServiceAuthError, String] =
    TokenCache.cache.getIfPresent(tokenKey)
      .map: token =>
        logger.info(s"Token from Cache: $tokenKey")
        Right(token)
      .getOrElse:
        tokenService.adminToken()
          .map: token =>
            logger.info(
              s"Added Token to Cache self acquired: $username - ${token.take(20)}...${token.takeRight(10)}"
            )
            TokenCache.cache.put(username, token)
            token

  def clientCredentialsToken()(using
      logger: WorkerLogger,
  ): Either[ServiceAuthError, String] =
    TokenCache.cache.getIfPresent("clientCredentials")
      .map: token =>
        logger.info(s"Token from Cache: clientCredentials")
        Right(token)
      .getOrElse:
        tokenService.clientCredentialsToken()
          .map: token =>
            logger.info(
              s"Added Token to Cache self acquired: $client_id - ${token.take(20)}...${token.takeRight(10)}"
            )
            TokenCache.cache.put("clientCredentials", token)
            token

  def impersonateToken(username: String, adminToken: String)(using
      logger: WorkerLogger,
  ): IO[ServiceAuthError, String] =
    TokenCache.cache.getIfPresent(username)
      .map: token =>
        logger.info(s"Token from Cache: $username")
        ZIO.succeed(token)
      .getOrElse:
        tokenService.impersonateToken(username, adminToken)
          .map: token =>
            logger.info(
              s"Added Token to Cache self acquired: $username - ${token.take(20)}...${token.takeRight(10)}"
            )
            TokenCache.cache.put(username, token)
            token

  lazy val tokenRequestBody = Map(
    "grant_type"    -> grant_type_password,
    "client_id"     -> client_id,
    "client_secret" -> client_secret,
    "scope"         -> scope,
    "username"      -> username,
    "password"      -> password
  )

  lazy val clientCredentialsTokenRequestBody = Map(
    "grant_type"    -> grant_type_client_credentials,
    "client_id"     -> client_id,
    "client_secret" -> client_secret,
    "scope"         -> scope
  )

  lazy val impersonateTokenRequestBody = Map(
    "grant_type"    -> grant_type_impersonate,
    "client_id"     -> client_id,
    "client_secret" -> client_secret,
    "scope"         -> scope
    // "subject_token" -> adminToken,
    // "requested_subject" -> username
  )

end OAuthPasswordFlow

case class TokenResponse(
    access_token: String,
    scope: String,
    token_type: String,
    refresh_token: Option[String]
)
given InOutDecoder[TokenResponse] = deriveDecoder
