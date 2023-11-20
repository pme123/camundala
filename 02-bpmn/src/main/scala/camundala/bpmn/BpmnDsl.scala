package camundala
package bpmn

import camundala.domain.*
import scala.reflect.ClassTag

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

  def serviceTask[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      ServiceOut: Encoder: Decoder
  ](
      topicName: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      defaultServiceOutMock: MockedServiceResponse[ServiceOut],
      descr: Optable[String] = None
  ): ServiceTask[In, Out, ServiceOut] =
    ServiceTask(
      InOutDescr(topicName, in, out, descr.value),
      defaultServiceOutMock
    )

  def customTask[
    In <: Product : Encoder : Decoder : Schema,
    Out <: Product : Encoder : Decoder : Schema
  ](
     topicName: String,
     in: In = NoInput(),
     out: Out = NoOutput(),
     descr: Optable[String] = None
   ): CustomTask[In, Out] =
    CustomTask(
      InOutDescr(topicName, in, out, descr.value),
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
      Out <: DmnValueType: Encoder: Decoder: Schema: ClassTag
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
      out: CollectEntries[Out] = CollectEntries(Seq.empty[Int]),
      descr: Optable[String] = None
  ): DecisionDmn[In, CollectEntries[Out]] =
    /* require(
      out.isCollectEntries,
      "A collectEntries must look like `case class CollectEntries(result: Int*)`"
    )*/
    dmn(decisionDefinitionKey, in, out, descr.value)

  given toCollectEntries[Out <: DmnValueType: Encoder: Decoder: Schema]: Conversion[Seq[Out], CollectEntries[Out]] =
    CollectEntries(_)

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

  def messageEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): MessageEvent[Msg] =
    MessageEvent(
      messageName,
      InOutDescr(
        id.getOrElse(messageName),
        in,
        NoOutput(),
        msgNameDescr(messageName, descr)
      )
    )

  @deprecated("Use messageEvent.")
  def receiveMessageEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): MessageEvent[Msg] =
    messageEvent(messageName, in, id, descr)

  def signalEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): SignalEvent[Msg] =
    SignalEvent(
      messageName,
      InOutDescr(
        id.getOrElse(messageName),
        in,
        NoOutput(),
        msgNameDescr(messageName, descr)
      )
    )

  @deprecated("Use signalEvent.")
  def receiveSignalEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): SignalEvent[Msg] =
    signalEvent(messageName, in, id, descr)

  def timerEvent(
      title: String,
      descr: Optable[String] = None
  ): TimerEvent =
    TimerEvent(
      title,
      InOutDescr(title, descr = descr.value)
    )

  private def msgNameDescr(messageName: String, descr: Optable[String]) =
    val msgNameDescr = s"- _messageName_: `$messageName`"
    Some(descr.value.map(_ + s"\n$msgNameDescr").getOrElse(msgNameDescr))

end BpmnDsl