package camundala.camunda7.worker

import camundala.bpmn.{InputParams, ProcessOrExternalTask}
import camundala.worker.{TopicDefinition, WorkerSubscription2}
import org.camunda.bpm.client.spring.SpringTopicSubscription
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
import org.camunda.bpm.client.spring.impl.subscription.{SubscriptionConfiguration, SubscriptionPostProcessor}
import org.camunda.bpm.client.spring.impl.util.AnnotationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.*
import org.springframework.beans.factory.support.*
import org.springframework.core.`type`.AnnotatedTypeMetadata

import scala.jdk.CollectionConverters.*

class WorkerPostProcessor(
    springTopicSubscription: Class[_ <: SpringTopicSubscription]
) extends BeanDefinitionRegistryPostProcessor:
  @throws[BeansException]
  override def postProcessBeanDefinitionRegistry(
      registry: BeanDefinitionRegistry
  ): Unit =

    val listableBeanFactory = registry.asInstanceOf[ListableBeanFactory]
    val workerDsls = listableBeanFactory.getBeanNamesForType(classOf[CExternalTaskHandler])
    workerDsls.foreach(w => subscribeWorker(w, registry))
  end postProcessBeanDefinitionRegistry

  private def subscribeWorker(handlerBeanName: String, registry: BeanDefinitionRegistry) =
    val handlerBeanDefinition = registry.getBeanDefinition(handlerBeanName)
    findSubscriptionAnnotation(handlerBeanDefinition)
      .foreach { subscriptionAnnotation =>
        val topicDefinition = TopicDefinition(subscriptionAnnotation.value())
        val subscriptionConfiguration = fromHandler(topicDefinition)
        val subscriptionBeanDefinition =
          getBeanDefinition(handlerBeanName, subscriptionConfiguration)

        val subscriptionBeanName = handlerBeanName + "Subscription"
        registry.registerBeanDefinition(subscriptionBeanName, subscriptionBeanDefinition)
        logger.info(
          s"Registered $subscriptionBeanName: ${subscriptionBeanDefinition.getBeanClassName}"
        )
      }
  end subscribeWorker

  private def findSubscriptionAnnotation(
      beanDefinition: BeanDefinition
  ): Option[WorkerSubscription2] =
    beanDefinition match
      case annotatedBeanDefinition: AnnotatedBeanDefinition =>
        val metadata = Option(annotatedBeanDefinition.getFactoryMethodMetadata)
          .getOrElse(annotatedBeanDefinition.getMetadata)
        Some(AnnotationUtil.get(classOf[WorkerSubscription2], metadata))
      case _ =>
        logger.warn(s"No Annotation found for ${beanDefinition.getBeanClassName}")
        None
  end findSubscriptionAnnotation

  private def getBeanDefinition(
      beanName: String,
      subscriptionConfiguration: SubscriptionConfiguration
  ): BeanDefinition =
    BeanDefinitionBuilder
      .genericBeanDefinition(springTopicSubscription)
      .addPropertyReference("externalTaskHandler", beanName)
      .addPropertyValue("subscriptionConfiguration", subscriptionConfiguration)
      .setDestroyMethodName("closeInternally")
      .getBeanDefinition
  end getBeanDefinition

  /** setup the SubscriptionConfiguration from the handler itself (not the annotation)
    */
  private def fromHandler(topicDefinition: TopicDefinition): SubscriptionConfiguration =
    val subConfig = new SubscriptionConfiguration
    subConfig.setTopicName(topicDefinition.topicName) // worker.topic)
    subConfig.setAutoOpen(true)
    subConfig.setLockDuration(null)
    // register the variabales from the input object and the general variables
    subConfig.setVariableNames(
      (topicDefinition.inputVariableNames ++ InputParams.values.map(_.toString)).asJava
    )
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

end WorkerPostProcessor
