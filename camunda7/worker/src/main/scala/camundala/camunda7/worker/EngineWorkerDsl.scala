package camundala.camunda7.worker

import camundala.camunda7.worker.{CExternalTaskHandler, DefaultCamunda7Context}
import camundala.worker.{EngineContext, WorkerLogger}
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

trait EngineWorkerDsl extends CExternalTaskHandler:

  def getLogger(clazz: Class[?]): WorkerLogger =
    engineContext.getLogger(clazz)

  @Autowired()
  var engineContext: EngineContext = _

  lazy val workerConfig: WorkerConfig = WorkerConfig()
  //TODO do this configurable
  private lazy val backoffStrategy =
    new ExponentialBackoffStrategy(
      workerConfig.backoffInitTimeInMs,
      workerConfig.backoffLockFactor,
      workerConfig.backoffLockMaxTimeInMs)

  private lazy val externalTaskClient: ExternalTaskClient =
    new ExternalTaskClientBuilderImpl()
      .baseUrl(workerConfig.processEngineRestUrl)
      .backoffStrategy(backoffStrategy)
      .lockDuration(workerConfig.workerLockDurationInMs)
      .workerId(s"$topic-worker")
      .build

  @PostConstruct
  def registerHandler(): Unit =
    externalTaskClient
      .subscribe(topic)
      .handler(this)
      .open()
    println(s"registerHandler: $engineContext")
  end registerHandler

end EngineWorkerDsl

