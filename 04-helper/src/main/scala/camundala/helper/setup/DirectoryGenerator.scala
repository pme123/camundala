package camundala.helper.setup

case class DirectoryGenerator()(using config: SetupConfig):
  lazy val generate =
    os.makeDir.all(config.sbtProjectDir)
    config.modules.map:
      generateModule

  

  private def generateModule(moduleConfig: ModuleConfig): Unit =

    def printMainAndTest(
        subProject: Option[String] = None,
    ): Unit =
      def modulePath(mainOrTest: String) =
        config.projectDir /
          moduleConfig.packagePath(config.projectPath, mainOrTest, subProject)
      end modulePath

      if moduleConfig.hasMain then
        os.makeDir.all(modulePath("main"))
      if moduleConfig.hasTest then
        os.makeDir.all(modulePath("test"))
    end printMainAndTest

    if config.subProjects.nonEmpty
    then
      config.subProjects
        .foreach: sp =>
          printMainAndTest(Some(sp))
    else printMainAndTest()
    end if

  end generateModule
end DirectoryGenerator
