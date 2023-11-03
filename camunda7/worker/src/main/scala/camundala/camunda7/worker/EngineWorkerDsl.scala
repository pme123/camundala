package camundala.camunda7.worker

import camundala.camunda7.worker.{CExternalTaskHandler, DefaultCamunda7Context}
import camundala.worker.{EngineContext, WorkerLogger}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

trait EngineWorkerDsl extends CExternalTaskHandler:

  def getLogger(clazz: Class[?]): WorkerLogger =
    engineContext.getLogger(clazz)

  @Autowired()
  var engineContext: EngineContext = _

end EngineWorkerDsl

