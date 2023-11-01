package camundala.worker

import camundala.camunda7.worker.DefaultCamunda7Context

class EngineWorkerDsl extends WorkerDsl :
  lazy val engineContext: EngineContext = DefaultCamunda7Context()
end EngineWorkerDsl

