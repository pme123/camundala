package camundala.helper.setup

case class BpmnGenerator()(using config: SetupConfig):

  def createProcess(processName: String): Unit =
    val name = processName.head.toUpper + processName.tail
    os.write.over(
      bpmnPath (processName) / s"$name.scala",
      process(processName, name)
    )
  end createProcess

  def createCustomWorker(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath (processName) / s"$bpmnName.scala",
      customWorker(processName, bpmnName)
    )
  end createCustomWorker

  private def process(
      processName: String,
      name: String
  ) =
    s"""package ${config.projectPackage}
       |package bpmn
       |package $processName
       |
       |object $name extends ${config.projectShortClassName}ProcessDsl:
       |
       |  val processName = "${config.projectName}-$processName"
       |  lazy val descr = ""
       |
       |  case class In(
       |  )
       |  object In:
       |    given ApiSchema[In] = deriveApiSchema
       |    given InOutCodec[In] = deriveInOutCodec
       |
       |  case class InConfig(
       |  )
       |  object InConfig:
       |    given ApiSchema[InConfig] = deriveApiSchema
       |    given InOutCodec[InConfig] = deriveInOutCodec
       |
       |  case class Out(
       |  )
       |  object Out:
       |    given ApiSchema[Out] = deriveApiSchema
       |    given InOutCodec[Out] = deriveInOutCodec
       |
       |  lazy val example = process(
       |    In(),
       |    Out()
       |  )
       |
       |end $name""".stripMargin

  private def customWorker(
      processName: String,
      name: String
  ) =
    s"""package ${config.projectPackage}
       |package bpmn.$processName
       |
       |object $name extends CompanyBpmnCustomWorkerDsl:
       |
       |  val topicName = "${config.projectName}-$processName.$name"
       |  val descr: String = ""
       |
       |  case class In(
       |  )
       |  object In:
       |    given ApiSchema[In] = deriveApiSchema
       |    given InOutCodec[In] = deriveInOutCodec
       |
       |  case class Out(
       |  )
       |  object Out:
       |    given ApiSchema[Out] = deriveApiSchema
       |    given InOutCodec[Out] = deriveInOutCodec
       |
       |  lazy val example = customTask(
       |    In(),
       |    Out()
       |  )
       |
       |end $name""".stripMargin

  private def bpmnPath(processName: String) =
    val dir = config.projectDir / ModuleConfig.bpmnModule.packagePath(
      config.projectPath,
    ) / processName
    os.makeDir.all(dir)
    dir
  end bpmnPath

end BpmnGenerator
