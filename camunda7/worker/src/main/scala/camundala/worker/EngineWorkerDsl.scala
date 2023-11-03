package camundala.worker

import camundala.camunda7.worker.DefaultCamunda7Context

trait EngineWorkerDsl extends WorkerDsl :
  lazy val logger = engineContext.getLogger(getClass)

  lazy val engineContext: EngineContext = DefaultCamunda7Context()
end EngineWorkerDsl

