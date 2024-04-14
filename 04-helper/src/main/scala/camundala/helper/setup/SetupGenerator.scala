package camundala.helper.setup

case class SetupGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    DirectoryGenerator().generate
    println("The following Files were not updated! - if you want so add $doNotAdjust at the top of these file.")
    SbtGenerator().generate
    SbtSettingsGenerator().generate
    GenericFileGenerator().generate
    WorkerGenerator().generate
    HelperGenerator().generate
    ApiGenerator().generate
  end generate

  def createProcess(processName: String, version: Option[Int]): Unit =
    BpmnGenerator().createProcess(processName, version)
    BpmnProcessGenerator().createBpmn(processName, version)
    SimulationGenerator().createProcess(processName, version)
    WorkerGenerator().createProcess(processName, version)
  end createProcess

  def createProcessElement(setupObject: SetupElement): Unit =
    BpmnGenerator().createProcessElement(setupObject)
    WorkerGenerator().createProcessElement(setupObject)

  def createUserTask(setupObject: SetupElement): Unit =
    BpmnGenerator().createProcessElement(setupObject)

  def createDecision(setupObject: SetupElement): Unit =
    BpmnGenerator().createProcessElement(setupObject)

  def createEvent(setupObject: SetupElement): Unit =
    BpmnGenerator().createEvent(setupObject)

end SetupGenerator
