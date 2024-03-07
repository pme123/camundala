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
end SetupGenerator
