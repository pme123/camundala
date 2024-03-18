package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnDecisionDsl extends BpmnDsl:

  def decisionDefinitionKey: String

  // Use result strategy, like _singleEntry_, _collectEntries_, _singleResult_, _resultList_
  private def dmn[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema
  ](
      in: In,
      out: Out
  ): DecisionDmn[In, Out] =
    DecisionDmn[In, Out](
      InOutDescr(decisionDefinitionKey, in, out, Some(dmnDescr))
    )

  def singleEntry[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: DmnValueType: InOutEncoder: InOutDecoder: Schema: ClassTag
  ](
      in: In,
      out: Out
  ): DecisionDmn[In, SingleEntry[Out]] =
    /* require(
      out.isSingleEntry,
      "A singleEntry must look like `case class SingleEntry(result: DmnValueType)`"
    ) */
    dmn(in, SingleEntry(out))

  def collectEntries[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: DmnValueType: InOutEncoder: InOutDecoder: Schema
  ](
      in: In,
      out: CollectEntries[Out] = CollectEntries(Seq.empty[Int])
  ): DecisionDmn[In, CollectEntries[Out]] =
    /* require(
      out.isCollectEntries,
      "A collectEntries must look like `case class CollectEntries(result: Int*)`"
    )*/
    dmn(in, out)

  given toCollectEntries[Out <: DmnValueType: InOutEncoder: InOutDecoder: Schema]
      : Conversion[Seq[Out], CollectEntries[Out]] =
    CollectEntries(_)

  def singleResult[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema
  ](
      in: In,
      out: Out
  ): DecisionDmn[In, SingleResult[Out]] =
    /*  require(
      out.isSingleResult,
      """A singleResult must look like `case class SingleResult(result: ManyOutResult)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        |> a case class with more than one `DmnValueType`s.
        |""".stripMargin
    ) */
    dmn(in, SingleResult(out))

  def resultList[
      In <: Product: InOutEncoder: InOutDecoder: Schema,
      Out <: Product: InOutEncoder: InOutDecoder: Schema
  ](
      in: In,
      out: Seq[Out]
  ): DecisionDmn[In, ResultList[Out]] =
    /*  require(
      out.isResultList,
      """A resultList must look like `case class ResultList(results: ManyOutResult*)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        | > a case class with more than one `DmnValueType`s.
        |""".stripMargin
    )*/
    dmn(in, ResultList(out))

  private lazy val dmnDescr =
    s"""
       |$descr
       |
       |---
       |Choose Type DMN in Business Rule Task:
       |
       |- Decision Reference: `$decisionDefinitionKey`
       |
       |$companyDescr
       |""".stripMargin
end BpmnDecisionDsl
