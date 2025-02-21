package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnProcessDsl extends BpmnDsl:

  def processName: String
  def processLabels: ProcessLabels = ProcessLabels.none

  def process[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema},
      InitIn <: Product: {InOutEncoder, Schema}
  ](
      in: In = NoInput(),
      out: Out = NoOutput(),
      initIn: InitIn = NoInput()
  ): Process[In, Out, InitIn] =
    Process(InOutDescr(processName, in, out, Some(description)), initIn, processLabels)

  def process[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema}
  ](
      in: In,
      out: Out
  ): Process[In, Out, NoInput] =
    process(in, out, NoInput())

  private lazy val description: String =
    s"""|
        |$descr
        |
        |---
        |
        |- **Called Element**: `$processName` (to define in the Call Activity)
        |
        |${processLabels.print}
        |
        |---
        |
        |$companyDescr
        |""".stripMargin

  // Use result strategy, like _singleEntry_, _collectEntries_, _singleResult_, _resultList_
  private def dmn[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema}
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Optable[String] = None
  ): DecisionDmn[In, Out] =
    DecisionDmn[In, Out](
      InOutDescr(decisionDefinitionKey, in, out, descr.value)
    )

  @deprecated("Use .. extends BpmnDecisionDsl")
  def singleEntry[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: DmnValueType: {InOutEncoder, InOutDecoder, Schema, ClassTag}
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

  @deprecated("Use .. extends BpmnDecisionDsl")
  def collectEntries[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: DmnValueType: {InOutEncoder, InOutDecoder, Schema}
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

  given toCollectEntries[
      Out <: DmnValueType: {InOutEncoder, InOutDecoder, Schema}
  ]
      : Conversion[Seq[Out], CollectEntries[Out]] =
    CollectEntries(_)

  @deprecated("Use .. extends BpmnDecisionDsl")
  def singleResult[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema}
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

  @deprecated("Use .. extends BpmnDecisionDsl")
  def resultList[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema}
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

  @deprecated("Use .. extends BpmnUserTaskDsl")
  def userTask[
      In <: Product: {InOutEncoder, InOutDecoder, Schema},
      Out <: Product: {InOutEncoder, InOutDecoder, Schema}
  ](
      id: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Optable[String] = None
  ): UserTask[In, Out] =
    UserTask(
      InOutDescr(id, in, out, descr.value)
    )

  @deprecated("Use .. extends BpmnMessageEventDsl")
  def messageEvent[
      Msg <: Product: {InOutEncoder, InOutDecoder, Schema}
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

  @deprecated("Use .. extends BpmnMessageEventDsl")
  def receiveMessageEvent[
    Msg <: Product: {InOutEncoder, InOutDecoder, Schema}
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): MessageEvent[Msg] =
    messageEvent(messageName, in, id, descr)

  @deprecated("Use .. extends BpmnSignalEventDsl")
  def signalEvent[
      Msg <: Product: {InOutEncoder, InOutDecoder, Schema}
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

  @deprecated("Use .. extends BpmnSignalEventDsl")
  def receiveSignalEvent[
      Msg <: Product: {InOutEncoder, InOutDecoder, Schema}
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Optable[String] = None
  ): SignalEvent[Msg] =
    signalEvent(messageName, in, id, descr)

  @deprecated("Use .. extends BpmnTimerDsl")
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

end BpmnProcessDsl
