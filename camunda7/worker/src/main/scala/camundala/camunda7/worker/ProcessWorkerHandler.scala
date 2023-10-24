package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.*
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

/** To avoid Annotations (Camunda Version specific), we extend
  * ExternalTaskHandler for required parameters.
  */
class ProcessWorkerHandler(
                           val worker: ProcessWorker[?,?]
) extends CExternalTaskHandler[ProcessWorker[?,?]] :
  lazy val topic: String = worker.topic

  println(s"ProcessWorkerHandler: $topic")


end ProcessWorkerHandler



class ServiceProcessWorkerHandler(
                                  val worker: ServiceProcessWorker[?, ?, ?, ?]
) extends CExternalTaskHandler[ServiceProcessWorker[?, ?, ?, ?]]:
  lazy val topic: String = worker.topic
  println(s"ServiceProcessWorkerHandler: $topic")


end ServiceProcessWorkerHandler
