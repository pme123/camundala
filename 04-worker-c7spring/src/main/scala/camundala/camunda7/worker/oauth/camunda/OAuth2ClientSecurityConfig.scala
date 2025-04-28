package camundala.camunda7.worker.oauth.camunda

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.{
  ClientRegistration,
  ClientRegistrationRepository
}

@Configuration
class OAuth2ClientSecurityConfig:

  /** Register an OAuth2AuthorizedClientManager @Bean and associate it with an
    * OAuth2AuthorizedClientProvider composite that provides support for the authorization_code,
    * refresh_token, client_credentials and password authorization grant types.
    *
    * See: https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html
    */
  @Bean
  def authorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository)
      : OAuth2AuthorizedClientManager =
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .authorizationCode()
      .refreshToken()
      .clientCredentials()
      .build()

    val authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

    authorizedClientManager
  end authorizedClientManager
end OAuth2ClientSecurityConfig
