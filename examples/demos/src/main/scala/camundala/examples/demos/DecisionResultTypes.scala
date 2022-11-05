package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

object DecisionResultTypes extends BpmnDsl:

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

  lazy val demoProcess = process(
    "just-for-demo"
  )

  given Schema[Input] = Schema.derived
  given Encoder[Input] = deriveEncoder
  given Decoder[Input] = deriveDecoder

  given Schema[ManyOutResult] = Schema.derived
  given Encoder[ManyOutResult] = deriveEncoder
  given Decoder[ManyOutResult] = deriveDecoder

  given Schema[BadManyOutResult] = Schema.derived
  given Encoder[BadManyOutResult] = deriveEncoder
  given Decoder[BadManyOutResult] = deriveDecoder

end DecisionResultTypes
