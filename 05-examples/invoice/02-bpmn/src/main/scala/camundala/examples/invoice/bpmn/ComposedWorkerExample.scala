package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.domain.*

// example for service API description
object ComposedWorkerExample extends BpmnCustomTaskDsl:

  final val topicName = "example-composedWorker"
  val descr = "Example to compose Workers."

  case class In(
      justDoIt: Option[Boolean] = Some(true)
  )
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
      people: Seq[People] = Seq(People()),
      success: Boolean = true
  )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  given InOutCodec[List[String]] = deriveCodec

  lazy val example: CustomTask[In, Out] =
    customTask(
      in = In(),
      out = Out()
    )

end ComposedWorkerExample
