package camundala
package bpmn

trait BpmnDsl:

  def process[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Optable[String] = None
  ): Process[In, Out] =
    Process(
      InOutDescr(id, in, out, descr.value)
    )

  def userTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Optable[String] = None
  ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, in, out, descr.value)
    )

  def callActivity[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      subProcessId: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Optable[String] = None
  ): CallActivity[In, Out] =
    CallActivity(
      subProcessId,
      InOutDescr(id, in, out, descr.value)
    )

  // Use result strategy, like _singleEntry_, _collectEntries_, _singleResult_, _resultList_
  private def dmn[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Optable[String] = None
  ): DecisionDmn[In, Out] =
    DecisionDmn[In, Out](
      InOutDescr(decisionDefinitionKey, in, out, descr.value)
    )

  def singleEntry[
      In <: Product: Encoder: Decoder: Schema,
      Out <: DmnValueType: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Optable[String] = None
  ): DecisionDmn[In, SingleEntry[Out]] =
   /* require(
      out.isSingleEntry,
      "A singleEntry must look like `case class SingleEntry(result: DmnValueType)`"
    ) */
    dmn(decisionDefinitionKey, in, SingleEntry(out), descr.value)

  def collectEntries[
      In <: Product: Encoder: Decoder: Schema,
      Out <: DmnValueType: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Seq[Out],
      descr: Optable[String] = None
  ): DecisionDmn[In, CollectEntries[Out]] =
   /* require(
      out.isCollectEntries,
      "A collectEntries must look like `case class CollectEntries(result: Int*)`"
    )*/
    dmn(decisionDefinitionKey, in, CollectEntries(out), descr.value)

  def singleResult[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Optable[String] = None
  ): DecisionDmn[In, SingleResult[Out]] =
  /*  require(
      out.isSingleResult,
      """A singleResult must look like `case class SingleResult(result: ManyOutResult)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        |> a case class with more than one `DmnValueType`s.
        |""".stripMargin
    ) */
    dmn(decisionDefinitionKey, in, SingleResult(out), descr.value)

  def resultList[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Seq[Out],
      descr: Optable[String] = None
  ): DecisionDmn[In, ResultList[Out]] =
  /*  require(
      out.isResultList,
      """A resultList must look like `case class ResultList(results: ManyOutResult*)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        | > a case class with more than one `DmnValueType`s.
        |""".stripMargin
    )*/
    dmn(decisionDefinitionKey, in, ResultList(out), descr.value)

  def serviceTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Optable[String] = None
  ): ServiceTask[In, Out] =
    ServiceTask(
      InOutDescr(id, in, out, descr.value)
    )
  def endEvent(
      id: String,
      descr: Optable[String] = None
  ): EndEvent =
    EndEvent(id, descr.value)

  def receiveMessageEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): ReceiveMessageEvent[Msg] =
    ReceiveMessageEvent(
      messageName,
      InOutDescr(id.getOrElse(messageName), in, NoOutput(), descr.value)
    )

  def receiveSignalEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): ReceiveSignalEvent[Msg] =
    ReceiveSignalEvent(
      messageName,
      InOutDescr(id.getOrElse(messageName), in, NoOutput(), descr.value)
    )

// Use this in the DSL to avoid Option[?]
// see https://stackoverflow.com/a/69925310/2750966
case class Optable[Out](value: Option[Out])

object Optable {
  implicit def fromOpt[T](o: Option[T]): Optable[T] = Optable(o)
  implicit def fromValue[T](v: T): Optable[T] = Optable(Some(v))
}