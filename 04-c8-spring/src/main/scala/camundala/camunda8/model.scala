package camundala
package camunda8

import domain.*
import bpmn.*
import io.circe.HCursor

case class CreateProcessInstanceIn[In, Out <: Product](
    // use the description of the object
    variables: In,
    @description(
      "If you have a Process that finishes within the timeout (10 seconds), you can define the Output class."
    )
    fetchVariables: Option[Class[Out]] = None
):
  def syncProcess(fetchVariables: Class[Out]): CreateProcessInstanceIn[In, Out] =
    copy(fetchVariables = Some(fetchVariables))
end CreateProcessInstanceIn

object CreateProcessInstanceIn:
  given [In: Decoder, Out <: Product: Decoder]: Decoder[CreateProcessInstanceIn[In, Out]] =
    deriveDecoder[CreateProcessInstanceIn[In, Out]]

  given [T <: Product: Decoder]: Decoder[Class[T]] =
    new Decoder[Class[T]]:
      final def apply(c: HCursor): Decoder.Result[Class[T]] =
        for className <- c.as[String]
        yield Class.forName(className).asInstanceOf[Class[T]]
end CreateProcessInstanceIn

case class CreateProcessInstanceOut[Out <: Product](
    processDefinitionKey: Long,
    bpmnProcessId: String,
    version: Int,
    processInstanceKey: Long,
    variables: Out
)

object CreateProcessInstanceOut:
  given [Out <: Product: Decoder]: Decoder[CreateProcessInstanceOut[Out]] =
    deriveDecoder[CreateProcessInstanceOut[Out]]

  given [Out <: Product: Encoder]: Encoder[CreateProcessInstanceOut[Out]] =
    deriveEncoder[CreateProcessInstanceOut[Out]]
end CreateProcessInstanceOut
