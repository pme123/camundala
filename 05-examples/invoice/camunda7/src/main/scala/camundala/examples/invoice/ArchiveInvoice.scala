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
    given ApiSchema[In] = deriveSchema
    given JsonCodec[In] = deriveCodec
  end In

  case class Out(
                  archived: Option[Boolean] = Some(true),
                )
  object Out:
    given ApiSchema[Out] = deriveSchema
    given JsonCodec[Out] = deriveCodec
  end Out

  given JsonCodec[List[String]] = deriveCodec

  lazy val example: CustomTask[In, Out] =
    customTask(
      topicName,
      in = In(),
      out = Out() ,
      descr = "Archives the Receipt.",
    )

end ArchiveInvoice

