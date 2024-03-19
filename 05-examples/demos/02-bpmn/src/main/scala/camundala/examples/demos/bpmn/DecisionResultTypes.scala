package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object DecisionResultTypes extends BpmnProcessDsl:
  val processName = "camundala-mapping-example"
  val descr = ""
  val companyDescr = ""

  case class Input(letter: String)

  // Many Output Parameter
  case class ManyOutResult(index: Int, emoji: String)

  lazy val singleEntryDMN = singleEntry(
    decisionDefinitionKey = "singleEntry",
    in = Input("A"),
    out = 1
  )

  lazy val singleResultDMN = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  lazy val collectEntriesDMN = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = Seq(1, 2)
  )

  lazy val resultListDMN = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = List(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )

  lazy val collectEntriesDMNEmptySeq = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("Z"),
    out = Seq.empty[Int]
  )

  lazy val resultListDMNEmptySeq = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("Z"),
    out = Seq.empty[ManyOutResult]
  )

  // bad cases
  case class BadManyOutResult(index: Int, manyOutResult: ManyOutResult)

  lazy val singleResultDMNBadOutput = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = BadManyOutResult(1, ManyOutResult(1, "🤩"))
  )

  lazy val resultListDMNBadOutput = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = Seq(
      BadManyOutResult(1, ManyOutResult(1, "🤩")),
      BadManyOutResult(1, ManyOutResult(2, "😂"))
    )
  )

  lazy val demoProcess = process()

  given ApiSchema[Input] = deriveApiSchema
  given InOutCodec[Input] = deriveCodec

  given ApiSchema[ManyOutResult] = deriveApiSchema
  given InOutCodec[ManyOutResult] = deriveCodec

  given ApiSchema[BadManyOutResult] = deriveApiSchema
  given InOutCodec[BadManyOutResult] = deriveCodec

end DecisionResultTypes
