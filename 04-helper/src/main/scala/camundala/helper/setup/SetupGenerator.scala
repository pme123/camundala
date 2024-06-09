package camundala.helper.setup

case class SetupGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    println(
      s"The following Files (red) were not updated! - if you want so add $doNotAdjust at the top of these file."
    )
    DirectoryGenerator().generate
    SbtGenerator().generate
    SbtSettingsGenerator().generate
    GenericFileGenerator().generate
    WorkerGenerator().generate
    DmnGenerator().generate
    ApiGeneratorGenerator().generate
    ApiGenerator().generate
  end generate

  def createProcess(setupElement: SetupElement): Unit =
    BpmnGenerator().createProcess(setupElement)
    BpmnProcessGenerator().createBpmn(setupElement)
    SimulationGenerator().createProcess(setupElement)
    WorkerGenerator().createProcess(setupElement)
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
