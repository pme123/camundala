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
      subProcessId: String,
      in: In = NoInput(),
      out: Out = NoOutput(),
      descr: Option[String] | String = None
  ): CallActivity[In, Out] =
    CallActivity(
      subProcessId,
      InOutDescr(id, in, out, descr)
    )

  def dmn[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Option[String] | String = None
  ): DecisionDmn[In, Out] =
    DecisionDmn[In, Out](
      InOutDescr(decisionDefinitionKey, in, out, descr)
    )

  def singleEntry[
      In <: Product: Encoder: Decoder: Schema,
      Out <: DmnValueType: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Option[String] | String = None
  ): DecisionDmn[In, SingleEntry[Out]] =
   /* require(
      out.isSingleEntry,
      "A singleEntry must look like `case class SingleEntry(result: DmnValueType)`"
    ) */
    dmn(decisionDefinitionKey, in, SingleEntry(out), descr)

  def collectEntries[
      In <: Product: Encoder: Decoder: Schema,
      Out <: DmnValueType: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Seq[Out],
      descr: Option[String] | String = None
  ): DecisionDmn[In, CollectEntries[Out]] =
   /* require(
      out.isCollectEntries,
      "A collectEntries must look like `case class CollectEntries(result: Int*)`"
    )*/
    dmn(decisionDefinitionKey, in, CollectEntries(out), descr)

  def singleResult[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Out,
      descr: Option[String] | String = None
  ): DecisionDmn[In, SingleResult[Out]] =
  /*  require(
      out.isSingleResult,
      """A singleResult must look like `case class SingleResult(result: ManyOutResult)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        |> a case class with more than one `DmnValueType`s.
        |""".stripMargin
    ) */
    dmn(decisionDefinitionKey, in, SingleResult(out), descr)

  def resultList[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      decisionDefinitionKey: String,
      in: In,
      out: Seq[Out],
      descr: Option[String] | String = None
  ): DecisionDmn[In, ResultList[Out]] =
  /*  require(
      out.isResultList,
      """A resultList must look like `case class ResultList(results: ManyOutResult*)`
        | with `case class ManyOutResult(index: Int, emoji: String)`
        | > a case class with more than one `DmnValueType`s.
        |""".stripMargin
    )*/
    dmn(decisionDefinitionKey, in, ResultList(out), descr)

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

  def receiveMessageEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Option[String] | String = None
  ): ReceiveMessageEvent[Msg] =
    ReceiveMessageEvent(
      messageName,
      InOutDescr(id.getOrElse(messageName), in, NoOutput(), descr)
    )

  def receiveSignalEvent[
      Msg <: Product: Encoder: Decoder: Schema
  ](
      messageName: String,
      in: Msg = NoInput(),
      id: Option[String] = None,
      descr: Option[String] | String = None
  ): ReceiveSignalEvent[Msg] =
    ReceiveSignalEvent(
      messageName,
      InOutDescr(id.getOrElse(messageName), in, NoOutput(), descr)
    )
