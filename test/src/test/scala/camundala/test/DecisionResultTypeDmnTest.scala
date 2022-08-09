package camundala
package test

import bpmn.*
import dmn.*
import org.junit.Test
import test.*
import os.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

class DecisionResultTypeDmnTest extends DmnTestRunner, BpmnDsl:

  val dmnPath: ResourcePath = baseResource / "DecisionResultTypes.dmn"

  case class Input(letter: String)
  // Many Output Parameter
  case class ManyOutResult(index: Int, emoji: String)
  case class BadManyOutResult(index: Int, manyOutResult: ManyOutResult)

  private lazy val singleEntryDMN = singleEntry(
    decisionDefinitionKey = "singleEntry",
    in = Input("A"),
    out = 1
  )

  private lazy val singleResultDMN = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = ManyOutResult(1, "🤩")
  )

  private lazy val collectEntriesDMN = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("A"),
    out = Seq(1, 2)
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
  def testSingleResultBadOutput(): Unit =
    test(singleResultDMNBadOutput)

  @Test
  def testCollectEntriesEmptySeq(): Unit =
    test(collectEntriesDMNEmptySeq)

  @Test(expected = classOf[AssertionError])
  def testResultListBadOutput(): Unit =
    test(resultListDMNBadOutput)

  @Test
  def testResultListEmptySeq(): Unit =
    test(resultListDMNEmptySeq)


  private def singleResultDMNBadOutput = singleResult(
    decisionDefinitionKey = "singleResult",
    in = Input("A"),
    out = BadManyOutResult(1, ManyOutResult(1, "🤩"))
  )

  private def collectEntriesDMNEmptySeq = collectEntries(
    decisionDefinitionKey = "collectEntries",
    in = Input("Z"),
    out = Seq.empty[Int]
  )

  private def resultListDMNBadOutput = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("A"),
    out = Seq(BadManyOutResult(1, ManyOutResult(1, "🤩")),BadManyOutResult(1, ManyOutResult(2, "😂")))
  )
  private def resultListDMNEmptySeq = resultList(
    decisionDefinitionKey = "resultList",
    in = Input("Z"),
    out = Seq.empty[ManyOutResult]
  )