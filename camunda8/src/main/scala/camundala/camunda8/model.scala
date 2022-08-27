package camundala
package camunda8

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

implicit def CreateProcessInstanceInDec[In: Decoder, Out <: Product: Decoder]
    : Decoder[CreateProcessInstanceIn[In, Out]] =
  deriveDecoder[CreateProcessInstanceIn[In, Out]]

implicit def ClassDec[T <: Product: Decoder]: Decoder[Class[T]] =
  new Decoder[Class[T]] {
    final def apply(c: HCursor): Decoder.Result[Class[T]] =
      for className <- c.as[String]
      yield Class.forName(className).asInstanceOf[Class[T]]
  }

case class CreateProcessInstanceOut[Out <: Product](
  processDefinitionKey: Long,
  bpmnProcessId: String,
  version: Int,
  processInstanceKey: Long,
  variables: Out
)

object CreateProcessInstanceOut:
  implicit def CreateProcessInstanceOutDec[Out <: Product: Decoder]
  : Decoder[CreateProcessInstanceOut[Out]] =
    deriveDecoder[CreateProcessInstanceOut[Out]]

  implicit def CreateProcessInstanceOutEnc[Out <: Product: Encoder]
  : Encoder[CreateProcessInstanceOut[Out]] =
    deriveEncoder[CreateProcessInstanceOut[Out]]
