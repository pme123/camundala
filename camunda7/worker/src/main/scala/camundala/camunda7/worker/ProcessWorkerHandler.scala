package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.ProcessWorker
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


/*
class ServiceProcessWorkerHandler(
                                   worker: ServiceProcessWorker[?, ?, ?, ?]
) extends CExternalTaskHandler:
  lazy val topic: String = worker.topic
  protected lazy val prototype: Any = worker.process.in
  println(s"ServiceProcessWorkerHandler: $topic")


end ServiceProcessWorkerHandler
*/