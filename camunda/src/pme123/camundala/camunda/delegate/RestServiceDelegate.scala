package pme123.camundala.camunda.delegate

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Service
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import zio.Runtime.default.unsafeRun
import zio.logging

/**
  * Provide a generic REST service
  */
@Service("restService")
class RestServiceDelegate
  extends CamundaDelegate {
  def execute(execution: DelegateExecution): Unit = {
    val logger = ModelLayers.logLayer("RestServiceDelegate")
    unsafeRun(
      (for {
        _ <- logging.log.info("Hello from Generic Rest Service again")
      } yield ())
        .provideCustomLayer(logger)
    )
  }

}
object RestServiceDelegate {
  val expression: DelegateExpression = DelegateExpression("#{restService}")
}
