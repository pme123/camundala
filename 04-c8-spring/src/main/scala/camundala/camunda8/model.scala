package camundala
package camunda8

import camundala.bpmn.*
import camundala.domain.*
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
  given [In: InOutDecoder, Out <: Product: InOutDecoder]: InOutDecoder[CreateProcessInstanceIn[In, Out]] =
    deriveDecoder[CreateProcessInstanceIn[In, Out]]

  given [T <: Product: InOutDecoder]: InOutDecoder[Class[T]] =
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
  given [Out <: Product: InOutCodec]: InOutCodec[CreateProcessInstanceOut[Out]] =
    deriveInOutCodec
end CreateProcessInstanceOut
