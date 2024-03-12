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
      def srcPath(mainOrTest: String) =
        config.projectDir /
          moduleConfig.packagePath(config.projectPath, mainOrTest, subProject)
      end srcPath
      def resourcesPath(mainOrTest: String) =
        config.projectDir /
          moduleConfig.packagePath(config.projectPath, mainOrTest, subProject, isSourceDir = false)
      end resourcesPath

      if moduleConfig.hasMain then
        os.makeDir.all(srcPath("main"))
        os.makeDir.all(resourcesPath("main"))
      if moduleConfig.hasTest then
        os.makeDir.all(srcPath("test"))
        os.makeDir.all(resourcesPath("test"))
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
