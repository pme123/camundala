package camundala
package test

import bpmn.*
import org.junit.Test
import test.*
import os.{Path, ResourcePath}
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class DecisionResultTypeDmnTest extends DmnTestRunner, BpmnDsl:

  val dmnPath: ResourcePath = baseResource / "DecisionResultTypes.dmn"

  case class Input(letter: String)
  // Single Output Parameter
  case class SingleEntry(index: Int)
  case class CollectEntries(indexes: Int*)
  // Many Output Parameter
  case class ManyOutResult(index: Int, emoji: String)
  case class BadManyOutResult(index: Int, manyOutResult: ManyOutResult)

  private lazy val singleEntryDMN = singleEntry(
    decisionDefinitionKey = "singleEntry",
    in = Input("A"),
    out = SingleEntry(1)
  )

  private lazy val singleResultDMN = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  private lazy val collectEntriesDMN = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = CollectEntries(1, 2)
  )

  private lazy val resultListDMN = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = List(ManyOutResult(1, "🤩"), ManyOutResult(2, "😂"))
  )

  @Test
  def testSingleEntry(): Unit =
    test(singleEntryDMN)

  @Test
  def testSingleResult(): Unit =
    test(singleResultDMN)

  @Test
  def testCollectEntries(): Unit =
    test(collectEntriesDMN)

  @Test
  def testResultList(): Unit =
    test(resultListDMN  )

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleEntryBadOutput(): Unit =
    test(singleEntryDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testSingleResultBadOutput(): Unit =
    test(singleResultDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testCollectEntriesBadOutput(): Unit =
    test(collectEntriesDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testCollectEntriesEmptySeq(): Unit =
    test(collectEntriesDMNEmptySeq)

  @Test(expected = classOf[IllegalArgumentException])
  def testResultListBadOutput(): Unit =
    test(resultListDMNBadOutput)

  @Test(expected = classOf[IllegalArgumentException])
  def testResultListEmptySeq(): Unit =
    test(resultListDMNEmptySeq)

  private def singleEntryDMNBadOutput = singleEntry(
    decisionDefinitionKey = "singleEntry",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  private def singleResultDMNBadOutput = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = BadManyOutResult(1, ManyOutResult(1, "🤩"))
  )

  private def collectEntriesDMNBadOutput = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )
  private def collectEntriesDMNEmptySeq = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = CollectEntries()
  )

  private def resultListDMNBadOutput = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = Seq(BadManyOutResult(1, ManyOutResult(1, "🤩")))
  )
  private def resultListDMNEmptySeq = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = Seq.empty[ManyOutResult]
  )