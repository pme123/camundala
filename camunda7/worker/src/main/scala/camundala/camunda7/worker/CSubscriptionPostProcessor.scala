package camundala
package camunda7.worker

import camundala.bpmn.{InputParams}
import camundala.camunda7.worker.CSubscriptionPostProcessor.LOG
import camundala.domain.prettyString
import camundala.worker.*
import org.camunda.bpm.client.spring.SpringTopicSubscription
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration
import org.camunda.bpm.client.spring.impl.subscription.util.SubscriptionLoggerUtil
import org.camunda.bpm.client.spring.impl.util.LoggerUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.config.{BeanDefinition, ConfigurableListableBeanFactory}
import org.springframework.beans.factory.support.{BeanDefinitionBuilder, BeanDefinitionRegistry, BeanDefinitionRegistryPostProcessor}

import scala.jdk.CollectionConverters.*

/** Init the non-Annotated Camundala Workers.
  *
  * Adjusted from _org.camunda.bpm.client.spring.impl.subscription.SubscriptionPostProcessor_
  */
class CSubscriptionPostProcessor(
    springTopicSubscription: Class[_ <: SpringTopicSubscription]
) extends BeanDefinitionRegistryPostProcessor:

  @throws[BeansException]
  override def postProcessBeanDefinitionRegistry(
      registry: BeanDefinitionRegistry
  ): Unit =

    val listableBeanFactory = registry.asInstanceOf[ListableBeanFactory]

  //  val workerDsls = listableBeanFactory.getBeanNamesForType(classOf[CExternalTaskHandler])
  //  workerDsls.foreach(w => subscribeWorker(w, registry))

  end postProcessBeanDefinitionRegistry

  private def subscribeWorker(beanName: String, registry: BeanDefinitionRegistry) =
  //  logger.info(s"Workers: ${prettyString(worker)}")
//    val handler = workerHandler(workerDsl.worker, workerDsl.engineContext)
    val subscriptionConfiguration = fromHandler()
    val subscriptionBeanDefinition =
      getBeanDefinition(beanName, subscriptionConfiguration)
    val subscriptionBeanName = beanName + "Subscription"
    registry.registerBeanDefinition(
      subscriptionBeanName,
      subscriptionBeanDefinition
    )
    LOG.beanRegistered(
      subscriptionBeanName,
      beanName
    )
  end subscribeWorker

  protected def getBeanDefinition(
                                                    beanName: String,
                                                    subscriptionConfiguration: SubscriptionConfiguration
                                                  ): BeanDefinition =
    BeanDefinitionBuilder
    .genericBeanDefinition(springTopicSubscription)
    .addPropertyReference("externalTaskHandler", beanName)
    //.addPropertyValue("externalTaskHandler", externalTaskHandler)
    .addPropertyValue("subscriptionConfiguration", subscriptionConfiguration)
    .setDestroyMethodName("closeInternally")
    .getBeanDefinition
  end getBeanDefinition

  /** setup the SubscriptionConfiguration from the handler itself (not the annotation)
    */
  private def fromHandler(): SubscriptionConfiguration =
    val subConfig = new SubscriptionConfiguration
    subConfig.setTopicName("star-wars-api-people-detail") //worker.topic)
    subConfig.setAutoOpen(true)
    subConfig.setLockDuration(null)
   // subConfig.setVariableNames((worker.variableNames ++ InputParams.values.map(_.toString)).asJava)
    subConfig.setLocalVariables(false)
    subConfig.setBusinessKey(null)
    subConfig.setProcessDefinitionId(null)
    subConfig.setProcessDefinitionIdIn(null)
    subConfig.setProcessDefinitionKey(null)
    subConfig.setProcessDefinitionKeyIn(null)
    subConfig.setProcessDefinitionVersionTag(null)
    subConfig.setProcessVariables(null)
    subConfig.setWithoutTenantId(null)
    subConfig.setTenantIdIn(null)
    subConfig.setIncludeExtensionProperties(false)
    subConfig
  end fromHandler

  @throws[BeansException]
  override def postProcessBeanFactory(
      beanFactory: ConfigurableListableBeanFactory
  ): Unit = {}

  private lazy val logger = LoggerFactory.getLogger(getClass)
object CSubscriptionPostProcessor:
  protected val LOG: SubscriptionLoggerUtil = LoggerUtil.SUBSCRIPTION_LOGGER
end CSubscriptionPostProcessor
