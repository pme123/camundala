package pme123.camundala.camunda.delegate

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}
import zio.{IO, ZIO}

trait CamundaDelegate extends JavaDelegate {

  implicit class CamundaExecution(execution: DelegateExecution) {

    def stringVar(key: String): IO[Unit, String] =
      asString(execution.getVariable(key))
  }

  private def asString(variable: AnyRef): IO[Unit, String] =
    ZIO.fromOption(Option(variable).map(_.toString))

}
