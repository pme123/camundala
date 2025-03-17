package camundala.camunda7.worker.oauth.camunda

import camundala.worker.EngineContext
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}

/** Configuration class for setting up the request interceptor. It uses `WorkerAccessTokenProvider`
  * to get the access token and add it to the request headers.
  */
@Configuration
class InterceptorConfiguration @Autowired() (
    private val accessTokenSyncWorker: WorkerAccessTokenProvider,
    private val engineContext: EngineContext
):

  private val logger = engineContext.getLogger(classOf[InterceptorConfiguration])

  @Bean
  def interceptor(): ClientRequestInterceptor = context =>
    logger.debug("Request interceptor called!")
    val token = accessTokenSyncWorker.getAccessTokenSync
    context.addHeader("Authorization", token)

end InterceptorConfiguration
