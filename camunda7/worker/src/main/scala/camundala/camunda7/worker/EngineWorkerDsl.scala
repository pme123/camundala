package camundala.camunda7.worker

import camundala.camunda7.worker.*
import camundala.worker.*
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

trait EngineWorkerDsl extends WorkerDsl, CExternalTaskHandler:

  protected def workerConfig: WorkerConfig = WorkerConfig()

  protected def getLogger(clazz: Class[?]): WorkerLogger =
    engineContext.getLogger(clazz)

  @Autowired()
  protected var engineContext: EngineContext = _

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

  def workerHandler(worker: Worker[?, ?, ?]) =
    worker match
      case pw: InitProcessWorker[?, ?] =>
        InitProcessWorkerHandler(pw, engineContext)
      case cw: CustomWorker[?, ?] =>
        CustomWorkerHandler(cw, engineContext)
      case spw: ServiceWorker[?, ?, ?, ?] =>
        ServiceWorkerHandler(spw, engineContext)
  end workerHandler

end EngineWorkerDsl

