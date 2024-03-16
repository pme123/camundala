package camundala.helper.setup

case class BpmnGenerator()(using config: SetupConfig):

  def createCustomWorker(processName: String, bpmnName: String): Unit =
    os.write.over(
      bpmnPath (processName) / s"$bpmnName.scala",
      customWorker(processName, bpmnName)
    )
  end createCustomWorker

  private def customWorker(
      processName: String,
      name: String
  ) =
    s"""package ${config.projectPackage}
       |package bpmn.$processName
       |
       |object $name extends CompanyBpmnCustomWorkerDsl:
       |
       |  val topicName: String = "${config.projectName}-$processName.$name"
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
