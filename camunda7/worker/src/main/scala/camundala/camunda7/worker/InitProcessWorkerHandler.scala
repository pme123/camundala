package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

/** To avoid Annotations (Camunda Version specific), we extend
  * ExternalTaskHandler for required parameters.
  */
class InitProcessWorkerHandler(
                           val worker: InitProcessWorker[?,?]
) extends CExternalTaskHandler[InitProcessWorker[?,?]] :
  lazy val topic: String = worker.topic

  println(s"ProcessWorkerHandler: $topic")


end InitProcessWorkerHandler

class CustomWorkerHandler(
                            val worker: CustomWorker[?,?]
                          ) extends CExternalTaskHandler[CustomWorker[?,?]] :
  lazy val topic: String = worker.topic

  println(s"CustomWorkerHandler: $topic")


end CustomWorkerHandler

class ServiceWorkerHandler(
                                  val worker: ServiceWorker[?, ?, ?, ?]
) extends CExternalTaskHandler[ServiceWorker[?, ?, ?, ?]]:
  lazy val topic: String = worker.topic
  println(s"ServiceWorkerHandler: $topic")


end ServiceWorkerHandler
