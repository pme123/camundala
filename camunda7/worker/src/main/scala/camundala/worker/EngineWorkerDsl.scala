package camundala.worker

import camundala.camunda7.worker.Camunda7Context

class EngineWorkerDsl extends WorkerDsl :
  given context: EngineContext = Camunda7Context()
end EngineWorkerDsl

