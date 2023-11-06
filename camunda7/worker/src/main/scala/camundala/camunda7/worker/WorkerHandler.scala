package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

/** To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
  * parameters.
  */
class InitProcessWorkerHandler(
    val worker: InitProcessWorker[?, ?], val engineContext: EngineContext
) extends CExternalTaskHandler:
  lazy val topic: String = worker.topic

  println(s"ProcessWorkerHandler: $topic")

end InitProcessWorkerHandler

class CustomWorkerHandler(
    val worker: CustomWorker[?, ?], val engineContext: EngineContext
) extends CExternalTaskHandler:
  lazy val topic: String = worker.topic

  println(s"CustomWorkerHandler: $topic")

end CustomWorkerHandler

class ServiceWorkerHandler(val worker: ServiceWorker[?, ?, ?, ?], val engineContext: EngineContext)
    extends CExternalTaskHandler:
  lazy val topic: String = worker.topic
  println(s"ServiceWorkerHandler: $topic")

end ServiceWorkerHandler
