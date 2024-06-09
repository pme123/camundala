package camundala.camunda

import org.camunda.spin.Spin.*

import io.circe.{Json, ParsingFailure, parser}
import org.camunda.bpm.engine.delegate.DelegateExecution

/** Check, if the variable `outputMock` is set and if it sets its values as process variables.
  */
trait Mocker:

  def mockOrProceed(execution: DelegateExecution): Unit =
    val outputMock = execution.getVariable("outputMock")
    val mocked =
      if Option(outputMock).isEmpty then false
      else
        val parsedJson: Either[ParsingFailure, Json] =
          parser.parse(outputMock.toString)
        println(s"Mocked Value: - $parsedJson")
        parsedJson match
          case Right(jsonObj) if jsonObj.isObject =>
            jsonObj.asObject.get.toMap
              .foreach { case k -> json =>
                execution.setVariable(k, camundaVariable(json))
              }
          case Right(other) =>
            throw new IllegalArgumentException(
              s"The mock must be a Json Object:\n- $other\n- ${other.getClass}"
            )
          case Left(exception) =>
            throw new IllegalArgumentException(
              s"The mock could not be parsed to Json Object:\n- $outputMock\n- $exception"
            )
        end match

        true
    execution.setVariable("mocked", mocked)
  end mockOrProceed

  private def camundaVariable(json: Json): Any =
    json match
      case j if j.isNull => null
      case j if j.isNumber => j.asNumber.get.toBigDecimal.get
      case j if j.isBoolean => j.asBoolean.get
      case j if j.isString => j.asString.get
      case j => JSON(j.toString)
  end camundaVariable

end Mocker
