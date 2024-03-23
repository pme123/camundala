package camundala.helper.setup

case class BpmnGenerator()(using config: SetupConfig):

  def createProcess(processName: String): Unit =
    val name = processName.head.toUpper + processName.tail
    os.write.over(
      bpmnPath(processName) / s"$name.scala",
      objectDefinition(
        "Process",
        processName,
        name,
        isProcess = true
      )
    )
  end createProcess

  def createCustomTask(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      objectDefinition(
        "CustomTask",
        processName,
        bpmnName
      )
    )
  end createCustomTask

  def createServiceTask(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      objectDefinition(
        "ServiceTask",
        processName,
        bpmnName
      )
    )
  end createServiceTask

  def createUserTask(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      objectDefinition(
        "UserTask",
        processName,
        bpmnName
      )
    )
  def createSignalEvent(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      eventDefinition(
        "Signal",
        processName,
        bpmnName
      )
    )
  def createMessageEvent(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      eventDefinition(
        "Message",
        processName,
        bpmnName
      )
    )
  def createTimerEvent(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath(processName) / s"$bpmnName.scala",
      eventDefinition(
        "Timer",
        processName,
        bpmnName
      )
    )

  private def objectDefinition(
      objectType: String,
      processName: String,
      name: String,
      isProcess: Boolean = false
  ) =
    s"""package ${config.projectPackage}
       |package bpmn.$processName
       |
       |object $name extends CompanyBpmn${objectType}Dsl:
       |
       |  val ${
        objectType match
          case "Process" => "processName"
          case "UserTask" => "name"
          case "Decision" => "decisionId"
          case "SignalEvent" | "MessageEvent" => "messageName"
          case "TimerEvent" => "title"
          case _ => "topicName"
      } = "${config.projectName}-$processName${
        if objectType == "Process" then "" else s".$name"
      }"
       |  val descr: String = ""
       |
       |${
        if objectType == "ServiceTask" then
          s"""  val path = "GET: my/path/TODO"
             |  type ServiceIn = NoInput
             |  type ServiceOut = NoOutput
             |  lazy val serviceInExample = NoInput()
             |  lazy val serviceMock = MockedServiceResponse.success200(NoOutput())
             |  """.stripMargin
        else ""
      }
       |${inOutDefinitions(isProcess)}
       |
       |  lazy val example = ${objectType.head.toLower + objectType.tail}(
       |    In(),
       |    Out()${
        if objectType == "ServiceTask" then
          s""",
             |    serviceMock,
             |    serviceInExample""".stripMargin
        else ""
      }
       |  )
       |end $name""".stripMargin

  private def eventDefinition(
      eventType: String,
      processName: String,
      name: String,
      isProcess: Boolean = false
  ) =
    s"""package ${config.projectPackage}
       |package bpmn.$processName
       |
       |object $name extends CompanyBpmn${eventType}EventDsl:
       |
       |  val ${
        if eventType == "Timer" then "title"
        else "messageName"
      } = "${config.projectName}-$processName.$name"
       |  val descr: String = ""
       |${
      if eventType == "Timer" then ""
      else """  case class In(
       |  )
       |  object In:
       |    given ApiSchema[In] = deriveApiSchema
       |    given InOutCodec[In] = deriveInOutCodec
       |""".stripMargin
    }
       |  lazy val example = ${eventType.head.toLower + eventType.tail}Event(${
        if eventType == "Timer" then ""
        else "In()"
      })
       |end $name""".stripMargin

  private def bpmnPath(processName: String) =
    val dir = config.projectDir / ModuleConfig.bpmnModule.packagePath(
      config.projectPath
    ) / processName
    os.makeDir.all(dir)
    dir
  end bpmnPath

  private def inOutDefinitions(isProcess: Boolean = false) =
    s"""  case class In(
      |  )
      |  object In:
      |    given ApiSchema[In] = deriveApiSchema
      |    given InOutCodec[In] = deriveInOutCodec
      |${
        if isProcess then
          """  case class InConfig(
            |  )
            |  object InConfig:
            |    given ApiSchema[InConfig] = deriveApiSchema
            |    given InOutCodec[InConfig] = deriveInOutCodec
            |""".stripMargin
        else ""
      }
      |  case class Out(
      |  )
      |  object Out:
      |    given ApiSchema[Out] = deriveApiSchema
      |    given InOutCodec[Out] = deriveInOutCodec"""

end BpmnGenerator
