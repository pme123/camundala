package camundala
package bpmn

import camundala.domain.*

case class Dmns(dmns: Seq[Dmn]):

  def :+(dmn: Dmn): Dmns = Dmns(dmns :+ dmn)

object Dmns:
  def none: Dmns = Dmns(Nil)

case class Dmn(path: Path, decisions: DecisionDmn[?,?]*)

type DmnValueType = String | Boolean | Int | Long | Double | scala.reflect.Enum

enum DecisionResultType:
  case singleEntry // TypedValue
  case singleResult // Map(String, Object)
  case collectEntries // List(Object)
  case resultList // List(Map(String, Object))

case class DecisionDmn[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode, InOut[In, Out, DecisionDmn[In, Out]]:

  override val label =
    """// use singleEntry / collectEntries / singleResult / resultList
      |  dmn""".stripMargin
  lazy val decisionDefinitionKey: String = inOutDescr.id

  def withInOutDescr(descr: InOutDescr[In, Out]): DecisionDmn[In, Out] =
    copy(inOutDescr = descr)

  def decisionResultType: DecisionResultType = {
    (inOutDescr.out) match
      case o: Product if o.isSingleEntry =>
        DecisionResultType.singleEntry
      case o: Product if o.isCollectEntries =>
        DecisionResultType.collectEntries
      case o: Product if o.isSingleResult =>
        DecisionResultType.singleResult
      case o: Product if o.isResultList =>
        DecisionResultType.resultList
  }
object DecisionDmn:

  def init(id: String): DecisionDmn[NoInput, NoOutput] =
    DecisionDmn(
      InOutDescr(id, NoInput(), NoOutput())
    )

extension (output: Product)

  def isSingleEntry =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case _: DmnValueType => true
        case _ => false
      )

  def isSingleResult =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case _: Iterable[?] => false
        case p: Product =>
          p.productIterator.size > 1
        case _ => false
      )

  def isCollectEntries: Boolean =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case p: Iterable[?] =>
          p.headOption match
            case Some(p: DmnValueType) => true
            case o => false
        case o => false
      )

  def isResultList =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case p: Iterable[?] =>
          p.headOption match
            case Some(p: Product) =>
              p.productIterator.size > 1
            case o => false
        case o => false
      )
  def hasManyOutputVars: Boolean =
    isSingleResult || isResultList
end extension // Product

