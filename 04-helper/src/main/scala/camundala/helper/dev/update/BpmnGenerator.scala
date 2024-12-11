package camundala.helper.dev.update

import camundala.helper.util.DevConfig

case class BpmnGenerator()(using config: DevConfig):

  def createProcess(setupElement: SetupElement): Unit =
    createIfNotExists(
      bpmnPath(setupElement.processName, setupElement.version) / s"${setupElement.bpmnName}.scala",
      objectDefinition(
        setupElement,
        isProcess = true
      )
    )
  end createProcess

  def createProcessElement(setupElement: SetupElement): Unit =
    val processName = setupElement.processName
    val version = setupElement.version
    createIfNotExists(
      bpmnPath(
        processName,
        version
      ) / s"${setupElement.bpmnName}.scala",
      objectDefinition(setupElement)
    )
    if setupElement.label == "ServiceTask"
    then
      val superTrait = processName.head.toUpper + processName.tail + s"V${version.getOrElse(1)}"
      createOrUpdate(
        bpmnPath(
          processName,
          version
        ) / s"$superTrait.scala",
        serviceTaskTrait(processName, version, superTrait)
      )
    end if
  end createProcessElement

  def createEvent(setupElement: SetupElement): Unit =
    createIfNotExists(
      bpmnPath(setupElement.processName, setupElement.version) / s"${setupElement.bpmnName}.scala",
      eventDefinition(setupElement)
    )

  private def objectDefinition(
      setupObject: SetupElement,
      isProcess: Boolean = false
  ) =
    val SetupElement(label, processName, bpmnName, version) = setupObject
    s"""package ${config.projectPackage}
       |package bpmn.$processName${version.versionPackage}
       |
       |object $bpmnName extends ${
        if label == "ServiceTask"
        then processName.head.toUpper + processName.tail + s"${version.versionLabel}:"
        else s"CompanyBpmn${label}Dsl:"
      }
       |
       |  val ${
        label match
          case "Process" => "processName"
          case "UserTask" => "name"
          case "Decision" => "decisionId"
          case "SignalEvent" | "MessageEvent" => "messageName"
          case "TimerEvent" => "title"
          case _ => "topicName"
      } = "${setupObject.identifier}"
       |  val descr: String = ""
       |
       |${
        if label == "ServiceTask" then
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
       |  lazy val example = ${
        if label == "Decision" then
          """singleResult( // singleEntry or collectEntries or  or resultList
            |    In(),
            |    Out() // Seq[Out] for collectEntries or  or resultList""".stripMargin
        else
          s"""${label.head.toLower + label.tail}(
             |    In(),
             |    Out()${if isProcess then ",\n    InitIn()" else ""}""".stripMargin
      }    ${
        if label == "ServiceTask" then
          s""",
             |    serviceMock,
             |    serviceInExample""".stripMargin
        else ""
      }
       |  )
       |end $bpmnName""".stripMargin
  end objectDefinition

  private def serviceTaskTrait(
      processName: String,
      version: Option[Int],
      superTrait: String
  ) =
    s"""package ${config.projectPackage}
       |package bpmn.$processName${version.versionPackage}
       |
       |object $superTrait:
       |
       |  final val serviceVersion = "${version.getOrElse(1)}.0"
       |  final val serviceLabel = s"${processName.head.toUpper + processName.tail} $$serviceVersion"
       |
       |  val description = ""
       |  val externalDoc = ""
       |  val externalUrl = ""
       |
       |trait $superTrait
       |  extends CompanyBpmnServiceTaskDsl:
       |  final val serviceLabel = $superTrait.serviceLabel
       |  val serviceVersion = $superTrait.serviceVersion
       |end $superTrait
       |""".stripMargin

  private def eventDefinition(
      setupObject: SetupElement
  ) =
    val SetupElement(label, processName, bpmnName, version) = setupObject
    s"""package ${config.projectPackage}
       |package bpmn.$processName${version.versionPackage}
       |
       |object $bpmnName extends CompanyBpmn${label}EventDsl:
       |
       |  val ${
        if label == "Timer" then "title"
        else "messageName"
      } = "${config.projectName}-$processName${setupObject.version.versionLabel}.$bpmnName"
       |  val descr: String = ""
       |${
        if label == "Timer" then ""
        else """  case class In(
              |  )
              |  object In:
              |    given ApiSchema[In] = deriveApiSchema
              |    given InOutCodec[In] = deriveInOutCodec
              |""".stripMargin
      }
       |  lazy val example = ${label.head.toLower + label.tail}Event(${
        if label == "Timer" then ""
        else "In()"
      })
       |end $bpmnName""".stripMargin
  end eventDefinition

  private def bpmnPath(processName: String, version: Option[Int]) =
    val subProject = config.subProjects.find(_ == processName)
    val dir = config.projectDir / ModuleConfig.bpmnModule.packagePath(
      config.projectPath,
      subProject = subProject
    ) / subProject.map(_ => os.rel).getOrElse(os.rel / processName) / version.versionPath
    os.makeDir.all(dir)
    dir
  end bpmnPath

  private def inOutDefinitions(isProcess: Boolean = false) =
    s"""  case class In(
      |     //TODO input variables
      |  ${
        if isProcess then
          """    @description(
            |        "A way to override process configuration.\n\n**SHOULD NOT BE USED on Production!**"
            |      )
            |      inConfig: Option[InConfig] = None
            |  ) extends WithConfig[InConfig]:
            |    lazy val defaultConfig = InConfig()
            |  end In""".stripMargin
        else """  )
        |  //type In = NoInput // if no input is needed
              |  """
      }
      |  object In:
      |    given ApiSchema[In] = deriveApiSchema
      |    given InOutCodec[In] = deriveInOutCodec
      |${
        if isProcess then
          """  case class InConfig(
            |    // Process Configuration
            |    // @description("To test cancel from other processes you need to set this flag.")
            |    //  waitForCancel: Boolean = false,
            |    // Mocks
            |    // outputServiceMock
            |    // @description(serviceOrProcessMockDescr(GetRelationship.serviceMock))
            |    // getRelationshipMock: Option[MockedServiceResponse[GetRelationship.ServiceOut]] = None,
            |    // outputMock
            |    // @description(serviceOrProcessMockDescr(GetContractContractKey.Out()))
            |    // getContractMock: Option[GetContractContractKey.Out] = None
            |  )
            |  object InConfig:
            |    given ApiSchema[InConfig] = deriveApiSchema
            |    given InOutCodec[InConfig] = deriveInOutCodec
            |
            |  //type InitIn = NoInput // if no initialisation is needed
            |  case class InitIn(
            |    //TODO init variables
            |  )
            |  object InitIn:
            |    given ApiSchema[InitIn] = deriveApiSchema
            |    given InOutCodec[InitIn] = deriveInOutCodec
            |
            |""".stripMargin
        else ""
      }
      |  case class Out(//TODO output variables
            ${//TODO output variables
        if isProcess then
          "        processStatus: ProcessStatus.succeeded.type = ProcessStatus.succeeded"
        else ""
      }
      |  )
      |  object Out:
      |    given ApiSchema[Out] = deriveApiSchema
      |    given InOutCodec[Out] = deriveInOutCodec"""

end BpmnGenerator
