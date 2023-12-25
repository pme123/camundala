package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.domain.*

// example for service API description
object ArchiveInvoice extends BpmnDsl:

  final val topicName = "ArchiveInvoiceService"

  case class  In(
                  shouldFail: Option[Boolean] = Some(true),
                )
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  case class Out(
                  archived: Option[Boolean] = Some(true),
                )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveCodec
  end Out

  given InOutCodec[List[String]] = deriveCodec

  lazy val example: CustomTask[In, Out] =
    customTask(
      topicName,
      in = In(),
      out = Out() ,
      descr = "Archives the Receipt.",
    )

end ArchiveInvoice

