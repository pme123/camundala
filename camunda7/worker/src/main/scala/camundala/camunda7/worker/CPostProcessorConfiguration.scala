package camundala.camunda7.worker

import org.camunda.bpm.client.spring.impl.subscription.SpringTopicSubscriptionImpl
import org.springframework.context.annotation.{Bean, Configuration}


/**
 * Add additional PostProcessor to init non-Annotated Workers.
 *
 * Adjusted from _org.camunda.bpm.client.spring.impl.PostProcessorConfiguration_
 */
@Configuration
class CPostProcessorConfiguration

object CPostProcessorConfiguration:


  @Bean
  def cSubscriptionPostprocessor: CSubscriptionPostProcessor = new CSubscriptionPostProcessor(classOf[SpringTopicSubscriptionImpl])

end CPostProcessorConfiguration
