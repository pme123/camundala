package camundala.camunda7.worker

import camundala.worker.WorkerDsl
import org.camunda.bpm.client.spring.impl.subscription.SpringTopicSubscriptionImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.stereotype.Component


/**
 * Add additional PostProcessor to init non-Annotated Workers.
 *
 * Adjusted from _org.camunda.bpm.client.spring.impl.PostProcessorConfiguration_
 */
@Configuration
class CPostProcessorConfiguration :
  @Bean
  def cSubscriptionPostprocessor = new CSubscriptionPostProcessor(classOf[SpringTopicSubscriptionImpl])

end CPostProcessorConfiguration
