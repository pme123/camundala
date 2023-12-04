package camundala.examples.invoice

import camundala.bpmn.*
import camundala.domain.*
import camundala.examples.invoice.ArchiveInvoice.*

// example for service API description
object ArchiveInvoice extends BpmnDsl:

  final val topicName = "ArchiveInvoiceService"

  case class  In(
                  shouldFail: Option[Boolean] = Some(true),
                )
  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  end In

  case class Out(
                  archived: Option[Boolean] = Some(true),
                )
  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  end Out

  given InOutCodec[List[String]] = deriveInOutCodec

  lazy val example: CustomTask[In, Out] =
    customTask(
      topicName,
      in = In(),
      out = Out() ,
      descr = "Archives the Receipt.",
    )

end ArchiveInvoice

