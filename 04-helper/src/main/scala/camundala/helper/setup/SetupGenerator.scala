package camundala.helper.setup

case class SetupGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    DirectoryGenerator().generate
    SbtGenerator().generate
    SbtSettingsGenerator().generate
    GenericFileGenerator().generate
    WorkerGenerator().generate
    HelperGenerator().generate
  end generate

  def createProcess(processName: String): Unit =
    BpmnGenerator().createProcess(processName)
    SimulationGenerator().createProcess(processName)
    WorkerGenerator().createProcess(processName)
  end createProcess

  def createCustomTask(processName: String, workerName: String): Unit =
    BpmnGenerator().createCustomTask(processName, workerName)
    WorkerGenerator().createCustomWorker(processName, workerName)
  def createServiceTask(processName: String, workerName: String): Unit =
    BpmnGenerator().createServiceTask(processName, workerName)
    WorkerGenerator().createServiceWorker(processName, workerName)

  def createUserTask(processName: String, workerName: String): Unit =
    BpmnGenerator().createUserTask(processName, workerName)

  def createSignalEvent(processName: String, workerName: String): Unit =
    BpmnGenerator().createSignalEvent(processName, workerName)
  def createMessageEvent(processName: String, workerName: String): Unit =
    BpmnGenerator().createMessageEvent(processName, workerName)
  def createTimerEvent(processName: String, workerName: String): Unit =
    BpmnGenerator().createTimerEvent(processName, workerName)

end SetupGenerator
