package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnUserTaskDsl extends BpmnDsl:

  def name: String

  def userTask[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema
  ](
      in: In = NoInput(),
      out: Out = NoOutput()
  ): UserTask[In, Out] =
    UserTask(
      InOutDescr(name, in, out, defaultDescr)
    )

end BpmnUserTaskDsl
