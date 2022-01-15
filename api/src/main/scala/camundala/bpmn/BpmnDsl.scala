package camundala
package bpmn

import domain.*

import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import io.circe.{Json, parser}

trait BpmnDsl:

  def process[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): Process[In, Out] =
    Process(
      InOutDescr(id, in, out, descr)
    )

  def userTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, in, out, descr)
    )

  def callActivity[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): CallActivity[In, Out] =
    CallActivity(
      InOutDescr(id, in, out, descr)
    )

  def dmn[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): DecisionDmn[In, Out] =
    DecisionDmn[In, Out](
      InOutDescr(decisionDefinitionKey, in, out, descr)
    )

  def singleEntry[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out
  ): DecisionDmn[In, Out] =
    require(
      out.isSingleEntry,
      "A singleEntry must look like `case class SingleEntry(result: DmnValueType)`"
    )
    dmn(decisionDefinitionKey, in, out)

  def collectEntries[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out
  ): DecisionDmn[In, Out] =
    require(
      out.isCollectEntries,
      "A collectEntries must look like `case class CollectEntries(indexes: Int*)`"
    )
    dmn(decisionDefinitionKey, in, out)

  def singleResult[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out
  ): DecisionDmn[In, Out] =
    require(
      out.isSingleResult,
      """A singleResult must look like `case class SingleResult(result: ManyOutResult)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        |""".stripMargin
    )
    dmn(decisionDefinitionKey, in, out)

  def resultList[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out
  ): DecisionDmn[In, Out] =
    require(
      out.isResultList,
      """A resultList must look like `case class ResultList(results: ManyOutResult*)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        |""".stripMargin
    )
    dmn(decisionDefinitionKey, in, out)

  def serviceTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): ServiceTask[In, Out] =
    ServiceTask(
      InOutDescr(id, in, out, descr)
    )
  def endEvent(
      id: String,
      descr: Option[String] | String = None
  ): EndEvent =
    EndEvent(id, descr)
