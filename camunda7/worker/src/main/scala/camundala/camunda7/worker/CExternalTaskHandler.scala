package camundala.camunda7.worker

import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

/**
 * To avoid Annotations (Camunda Version specific), we extend ExternalTaskHandler for required
 * parameters.
 */
trait CExternalTaskHandler extends ExternalTaskHandler :
  def topic: String

end CExternalTaskHandler
