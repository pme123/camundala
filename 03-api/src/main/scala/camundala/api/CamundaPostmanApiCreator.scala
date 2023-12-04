package camundala
package api

import camundala.bpmn.*
import camundala.domain.*
import sttp.tapir.EndpointIO.Example
import sttp.tapir.json.circe.*
import sttp.tapir.{PublicEndpoint, *}

trait CamundaPostmanApiCreator extends PostmanApiCreator:

  protected def createPostmanForProcess(
      api: ProcessApi[?, ?],
      tag: String,
      isGroup: Boolean = false
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.startProcess(tag, isGroup)
    )

  protected def createPostmanForExternalTask(
      api: ExternalTaskApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.startProcess(tag)
    )
  end createPostmanForExternalTask

  protected def createPostmanForUserTask(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.getActiveTask(tag),
      api.getTaskFormVariables(tag),
      api.completeTask(tag)
    )
  protected def createPostmanForDecisionDmn(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.evaluateDecision(tag)
    )
  protected def createPostmanForMessageEvent(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.correlateMessage(tag)
    )
  protected def createPostmanForSignalEvent(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.sendSignal(tag)
    )

  protected def createPostmanForTimerEvent(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.getActiveJob(tag),
      api.executeTimer(tag)
    )

  extension (process: ProcessApi[?, ?])
    def startProcess(
        tag: String,
        isGroup: Boolean
    ): PublicEndpoint[?, Unit, ?, Any] =
      process.inOut.startEventType match
        case StartEventType.Message =>
          correlateMessage(tag)
        case StartEventType.Signal =>
          sendSignal(tag)
        case _ =>
          startProcessNone(tag, isGroup)
    end startProcess

    private def startProcessNone(
        tag: String,
        isGroup: Boolean
    ): PublicEndpoint[?, Unit, ?, Any] =
      val path =
        tenantIdPath(
          "process-definition" / "key" / process.endpointPath(isGroup),
          "start"
        )
      val input =
        process
          .toPostmanInput((example: FormVariables) =>
            StartProcessIn(
              example,
              Some(process.name)
            )
          )
      process
        .postmanBaseEndpoint(tag, input, "StartProcess")
        .in(path)
        .post
    end startProcessNone

    private def correlateMessage(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "message" / s"--REMOVE:${process.id}--"
      val input = process
        .toPostmanInput((example: FormVariables) =>
          CorrelateMessageIn(
            process.id,
            Some(process.name),
            tenantId = apiConfig.tenantId,
            processVariables = Some(example)
          )
        )
      val descr =
        s"""
           |${process.descr}
           |
           |Message:
           |- _messageName_: `${process.id}`,
           |""".stripMargin
      process
        .postmanBaseEndpoint(tag, input, "CorrelateMessage", Some(descr))
        .in(path)
        .post
    end correlateMessage

    private def sendSignal(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "signal" / s"--REMOVE:${process.id}--"
      val input = process
        .toPostmanInput((example: FormVariables) =>
          SendSignalIn(
            process.id,
            tenantId = apiConfig.tenantId,
            variables = Some(example)
          )
        )
      val descr =
        s"""
           |${process.descr}
           |
           |Signal:
           |- _messageName_: `${process.id}`,
           |""".stripMargin
      process
        .postmanBaseEndpoint(tag, input, "SendSignal", Some(descr))
        .in(path)
        .post
    end sendSignal

  end extension

  extension (externalTaskApi: ExternalTaskApi[?, ?])

    def startProcess(
        tag: String
    ): PublicEndpoint[?, Unit, ?, Any] =
      val path =
        tenantIdPath(
          "process-definition" / "key" / externalTaskApi.processName,
          s"start--REMOVE:${externalTaskApi.id}--"
        )
      val input =
        externalTaskApi
          .toPostmanInput((example: FormVariables) =>
            StartProcessIn(
              example,
              Some(externalTaskApi.name)
            )
          )
      externalTaskApi
        .postmanBaseEndpoint(tag, input, "StartProcess")
        .in(path)
        .post
    end startProcess

    private def correlateMessage(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "message" / s"--REMOVE:${externalTaskApi.id}--"
      val input = externalTaskApi
        .toPostmanInput((example: FormVariables) =>
          CorrelateMessageIn(
            externalTaskApi.id,
            Some(externalTaskApi.name),
            tenantId = apiConfig.tenantId,
            processVariables = Some(example)
          )
        )
      val descr =
        s"""
           |${externalTaskApi.descr}
           |
           |Message:
           |- _messageName_: `${externalTaskApi.id}`,
           |""".stripMargin
      externalTaskApi
        .postmanBaseEndpoint(tag, input, "CorrelateMessage", Some(descr))
        .in(path)
        .post
    end correlateMessage

    private def sendSignal(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "signal" / s"--REMOVE:${externalTaskApi.id}--"
      val input = externalTaskApi
        .toPostmanInput((example: FormVariables) =>
          SendSignalIn(
            externalTaskApi.id,
            tenantId = apiConfig.tenantId,
            variables = Some(example)
          )
        )
      val descr =
        s"""
           |${externalTaskApi.descr}
           |
           |Signal:
           |- _messageName_: `${externalTaskApi.id}`,
           |""".stripMargin
      externalTaskApi
        .postmanBaseEndpoint(tag, input, "SendSignal", Some(descr))
        .in(path)
        .post
    end sendSignal

  end extension
  extension (api: ActivityApi[?, ?])

    def getActiveTask(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "task" / s"--REMOVE:${api.id}--"

      val input =
        api
          .toPostmanInput(_ => GetActiveTaskIn())
      api
        .postmanBaseEndpoint(tag, input, "GetActiveTask")
        .in(path)
        .post
    end getActiveTask

    def getTaskFormVariables(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path =
        "task" / taskIdPath() / "form-variables" / s"--REMOVE:${api.id}--"

      api
        .postmanBaseEndpoint(tag, None, "GetTaskFormVariables")
        .in(path)
        .in(
          query[String]("variableNames")
            .description(
              """A comma-separated list of variable names. Allows restricting the list of requested variables to the variable names in the list.
                |It is best practice to restrict the list of variables to the variables actually required by the form in order to minimize fetching of data. If the query parameter is ommitted all variables are fetched.
                |If the query parameter contains non-existent variable names, the variable names are ignored.""".stripMargin
            )
            .default(
              api.apiExamples.inputExamples.examples.head.productElementNames
                .mkString(",")
            )
        )
        .in(
          query[Boolean]("deserializeValues")
            .default(false)
        )
        .get

    def completeTask(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "task" / taskIdPath() / "complete" / s"--REMOVE:${api.id}--"

      val input = api
        .toPostmanInput(
          (example: FormVariables) => CompleteTaskIn(example),
          api.apiExamples.outputExamples.fetchExamples
        )

      api
        .postmanBaseEndpoint(tag, input, "CompleteTask")
        .in(path)
        .post

    def evaluateDecision(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val decisionDmn = api.inOut.asInstanceOf[DecisionDmn[?, ?]]
      val path = tenantIdPath(
        "decision-definition" / "key" / definitionKeyPath(
          decisionDmn.decisionDefinitionKey
        ) / s"--REMOVE:${api.id}--",
        "evaluate"
      )
      val input = api
        .toPostmanInput((example: FormVariables) => EvaluateDecisionIn(example))
      val descr = s"""
                     |${api.descr}
                     |
                     |Decision DMN:
                     |- _decisionDefinitionKey_: `${decisionDmn.decisionDefinitionKey}`,
                     |""".stripMargin
      api
        .postmanBaseEndpoint(tag, input, "EvaluateDecision", Some(descr))
        .in(path)
        .post

    def correlateMessage(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val event = api.inOut.asInstanceOf[MessageEvent[?]]
      val path = "message" / s"--REMOVE:${event.messageName}--"
      val input = api
        .toPostmanInput((example: FormVariables) =>
          CorrelateMessageIn(
            event.messageName,
            Some(api.name),
            tenantId = apiConfig.tenantId,
            processVariables = Some(example)
          )
        )
      val descr = s"""
                     |${api.descr}
                     |
                     |Message:
                     |- _messageName_: `${event.messageName}`,
                     |""".stripMargin
      api
        .postmanBaseEndpoint(tag, input, "CorrelateMessage", Some(descr))
        .in(path)
        .post

    def sendSignal(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val event = api.inOut.asInstanceOf[SignalEvent[?]]
      val path = "signal" / s"--REMOVE:${event.messageName}--"
      val input = api
        .toPostmanInput((example: FormVariables) =>
          SendSignalIn(
            event.messageName,
            tenantId = apiConfig.tenantId,
            variables = Some(example)
          )
        )
      val descr = s"""
                     |${api.descr}
                     |
                     |Signal:
                     |- _messageName_: `${event.messageName}`,
                     |""".stripMargin
      api
        .postmanBaseEndpoint(tag, input, "SendSignal", Some(descr))
        .in(path)
        .post

    def getActiveJob(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "job" / s"--REMOVE:${api.id}--"

      val input =
        api
          .toPostmanInput(_ => GetActiveJobIn())
      api
        .postmanBaseEndpoint(tag, input, "GetActiveJob")
        .in(path)
        .post
    end getActiveJob

    def executeTimer(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val event = api.inOut.asInstanceOf[TimerEvent]
      val path = "job" / jobIdPath() / "execute" / s"--REMOVE:${event.id}--"
      api
        .postmanBaseEndpoint(tag, None, "ExecuteTimer", Some(api.descr))
        .in(path)
        .post

  end extension

  extension (inOutApi: InOutApi[?, ?])
    def toPostmanInput[
        T <: Product:  InOutCodec: ApiSchema
    ](
        wrapper: FormVariables => T,
        examples: Seq[InOutExample[?]] = inOutApi.apiExamples.inputExamples.fetchExamples
    ): Option[EndpointInput[T]] =
      inOutApi.inOut.in match
        case _: NoInput =>
          None
        case _ =>
          Some(
            jsonBody[T]
              .examples(examples.map { case ex @ InOutExample(label, _) =>
                Example(
                  wrapper(ex.toCamunda),
                  Some(label),
                  None
                )
              }.toList)
          )
  end extension

end CamundaPostmanApiCreator
