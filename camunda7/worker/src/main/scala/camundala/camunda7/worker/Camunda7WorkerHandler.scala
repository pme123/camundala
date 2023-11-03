package camundala
package camunda7.worker

import camundala.worker.*
/*
/** To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
  * parameters.
  */
class InitProcessWorkerHandler(
    val worker: InitProcessWorker[?, ?],
    val engineContext: EngineContext
) extends CExternalTaskHandler[InitProcessWorker[?, ?]]:
  lazy val topic: String = worker.topic
  
end InitProcessWorkerHandler

class CustomWorkerHandler(
    val worker: CustomWorker[?, ?],
    val engineContext: EngineContext
)extends CExternalTaskHandler[CustomWorker[?, ?]]:
  lazy val topic: String = worker.topic
  
end CustomWorkerHandler

class ServiceWorkerHandler(
    val worker: ServiceWorker[?, ?, ?, ?],
    val engineContext: EngineContext
) extends CExternalTaskHandler[ServiceWorker[?, ?, ?, ?]]:
  lazy val topic: String = worker.topic

end ServiceWorkerHandler
*/