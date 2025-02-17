package camundala.camunda7.worker.oauth.camunda

import camundala.worker.EngineContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*

import java.util.Objects

/** Provides access tokens for the worker. Uses Standard spring OAuth2AuthorizedClientManager to get
  * the access token.
  */
//TODO unify with other access token -> KeycloakAccessTokenProvider
//TODO: as Token expires after 1 minute, caching is not useful. -> Is it possible to increase the expiration time?
// at the moment every 10 seconds a new token is requested
@Configuration
class WorkerAccessTokenProvider @Autowired() (
    private val authorizedClientManager: OAuth2AuthorizedClientManager,
    private val engineContext: EngineContext
):

  private val logger = engineContext.getLogger(classOf[WorkerAccessTokenProvider])

  def getAccessTokenSync: String =
    val authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("worker")
      .principal("Worker")
      .build()
    val authorizedClient = this.authorizedClientManager.authorize(authorizeRequest)
    val accessToken      = Objects.requireNonNull(authorizedClient).getAccessToken
    val token            = accessToken.getTokenValue
    logger.debug(
      s"""Got Camunda Token:
         |- Scopes: ${accessToken.getScopes}
         |- Token: ${token.take(20)}...${token.takeRight(10)}""".stripMargin
    )
    "Bearer " + token
  end getAccessTokenSync
end WorkerAccessTokenProvider
