package pme123.camundala.camunda.delegate

import org.camunda.bpm.engine.delegate.{DelegateExecution, JavaDelegate}
import zio.{IO, ZIO}

trait CamundaDelegate extends JavaDelegate {

  implicit class CamundaExecution(execution: DelegateExecution) {

    def stringVar(key: String): IO[Option[Nothing], String] =
      asString(execution.getVariable(key))
  }

  private def asString(variable: AnyRef): IO[Option[Nothing], String] =
    ZIO.fromOption(Option(variable).map(_.toString))

}
