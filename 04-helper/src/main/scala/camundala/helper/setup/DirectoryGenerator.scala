package camundala.helper.setup

case class DirectoryGenerator()(using config: SetupConfig):
  lazy val generate =
    os.makeDir.all(config.sbtProjectDir)
    config.modules.map:
      generateModule

  

  private def generateModule(moduleConfig: ModuleConfig): Unit =

    def printMainAndTest(
        subProject: Option[String] = None,
        generateSubModule: Boolean = false
    ): Unit =
      def modulePath(mainOrTest: String) =
        val subPackage = subProject.toSeq
        val subModule = if generateSubModule then subPackage else Seq.empty
        config.projectDir /
          moduleConfig.nameWithLevel /
          subModule / "src" / mainOrTest / "scala" /
          config.projectPath / moduleConfig.name / subPackage
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
          printMainAndTest(Some(sp), moduleConfig.generateSubModule)
    else printMainAndTest()
    end if

  end generateModule
end DirectoryGenerator
