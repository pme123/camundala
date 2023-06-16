package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.spin.Spin.*

import io.circe.{Json, JsonObject, ParsingFailure, parser}
import org.camunda.bpm.engine.delegate.{
  BpmnError,
  DelegateExecution,
  ExecutionListener
}
import org.camunda.bpm.engine.variable.`type`.{
  FileValueType,
  PrimitiveValueType,
  SerializableValueType,
  ValueType
}
import org.camunda.bpm.engine.variable.impl.`type`.PrimitiveValueTypeImpl.StringTypeImpl
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl
import org.camunda.bpm.engine.variable.value.TypedValue
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*

trait Mocker:

  def mockOrProceed(execution: DelegateExecution): Unit =
    val outputMock = execution.getVariable("outputMock")
    println(s"outputMock: $outputMock")
    val outputMockStr =
      s"is $outputMock" // only way outputMock does not throw a NullpointerException??!!
    val mocked =
      if (null == outputMock) false
      else
        val parsedJson: Either[ParsingFailure, Json] =
          parser.parse(outputMock.toString)
        println(s"Mocked Value: - $parsedJson")
        parsedJson match
          case Right(jsonObj) if jsonObj.isObject =>
            jsonObj.asObject.get.toMap
              .foreach { case k -> json =>
                println(s"setVariable: $k: $json")
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

        true
    println(s"mocked: $mocked")

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
