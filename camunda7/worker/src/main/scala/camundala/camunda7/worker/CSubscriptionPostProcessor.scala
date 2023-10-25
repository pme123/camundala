package camundala
package camunda7.worker

import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.spring.SpringTopicSubscription
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
import org.camunda.bpm.client.spring.impl.client.util.ClientLoggerUtil
import org.camunda.bpm.client.spring.impl.subscription.SubscriptionConfiguration
import org.camunda.bpm.client.spring.impl.subscription.util.SubscriptionLoggerUtil
import org.camunda.bpm.client.spring.impl.util.{AnnotationUtil, LoggerUtil}
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.{AnnotatedBeanDefinition, Autowired}
import org.springframework.beans.factory.config.{BeanDefinition, ConfigurableListableBeanFactory}
import org.springframework.beans.factory.support.{BeanDefinitionBuilder, BeanDefinitionRegistry, BeanDefinitionRegistryPostProcessor}
import org.springframework.core
import org.springframework.core.`type`.{AnnotatedTypeMetadata, MethodMetadata}

import scala.jdk.CollectionConverters.*

/**
 * Init the non-Annotated Camundala Workers.
 *
 * Adjusted from _org.camunda.bpm.client.spring.impl.subscription.SubscriptionPostProcessor_
 */
class CSubscriptionPostProcessor(
    springTopicSubscription: Class[_ <: SpringTopicSubscription],
) extends BeanDefinitionRegistryPostProcessor:

  @throws[BeansException]
  override def postProcessBeanDefinitionRegistry(
      registry: BeanDefinitionRegistry
  ): Unit =

    val listableBeanFactory = registry.asInstanceOf[ListableBeanFactory]
    val workerDsl = listableBeanFactory.getBean(classOf[WorkerDsl])
    println(s"WORKERDSL: ${workerDsl.workers}")
    val handlerBeans = workerDsl.workers.workers.map(w => w.topic -> workerHandler(w))

    for (handlerBean <- handlerBeans) {
      val (handlerBeanName -> handler) = handlerBean
      val subscriptionConfiguration = fromHandler(handler)
      val subscriptionBeanDefinition =
        getBeanDefinition(handler, subscriptionConfiguration)
      val subscriptionBeanName = handlerBeanName + "Subscription"
      registry.registerBeanDefinition(
        subscriptionBeanName,
        subscriptionBeanDefinition
      )
      CSubscriptionPostProcessor.LOG.beanRegistered(
        subscriptionBeanName,
        handlerBeanName
      )
    }
  end postProcessBeanDefinitionRegistry

  protected def getBeanDefinition(
                                   externalTaskHandler: CExternalTaskHandler[?],
      subscriptionConfiguration: SubscriptionConfiguration
  ): BeanDefinition = BeanDefinitionBuilder
    .genericBeanDefinition(springTopicSubscription)
    .addPropertyValue("externalTaskHandler", externalTaskHandler)
    .addPropertyValue("subscriptionConfiguration", subscriptionConfiguration)
    .setDestroyMethodName("closeInternally")
    .getBeanDefinition

  /**
   * setup the SubscriptionConfiguration from the handler itself (not the annotation)
   */
  private def fromHandler(handler: CExternalTaskHandler[?]): SubscriptionConfiguration =
    val subConfig = new SubscriptionConfiguration
    subConfig.setTopicName(handler.topic)
    subConfig.setAutoOpen(true)
    subConfig.setLockDuration(null)
    subConfig.setVariableNames(handler.variableNames.asJava)
    subConfig.setLocalVariables(false)
    subConfig.setBusinessKey(null)
    subConfig.setProcessDefinitionId(null)
    subConfig.setProcessDefinitionIdIn(null)
    subConfig.setProcessDefinitionKey(null)
    subConfig.setProcessDefinitionKeyIn(null )
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


  def workerHandler(worker: Worker[?, ?,?]) =
      worker match
        case pw: ProcessWorker[?,?] =>
          ProcessWorkerHandler(pw)
        case spw: ServiceWorker[?, ?, ?, ?] =>
          ServiceProcessWorkerHandler(spw)

object CSubscriptionPostProcessor:
  protected val LOG: SubscriptionLoggerUtil = LoggerUtil.SUBSCRIPTION_LOGGER
end CSubscriptionPostProcessor
