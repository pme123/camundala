package camundala.helper.setup

import camundala.BuildInfo
import camundala.api.docs.ApiProjectConf
import camundala.helper.util.ReposConfig

case class SetupConfig(
    projectName: String,
    baseDir: os.Path = os.pwd,
    modules: Seq[ModuleConfig] = SetupConfig.modules,
    subProjects: Seq[String] = Seq.empty,
    apiProjectConf: ApiProjectConf,
    versionConfig: VersionConfig = VersionConfig(),
    reposConfig: ReposConfig = ReposConfig.dummyRepos,
    sbtDockerSettings: String = ""
):
  lazy val companyName = apiProjectConf.org
  lazy val projectDir: os.Path = SetupConfig.projectDir(projectName, baseDir)

  lazy val projectPackage: String = projectName.split("-").mkString(".")
  lazy val projectPath: os.RelPath = os.rel / projectName.split("-")
  lazy val sbtProjectDir: os.Path = projectDir / "project"

  def dependsOn(level: Int): String =
    val depsOn = modules
      .filter(_.level == level - 1) // depends on one level above
      .map(_.name)
    if depsOn.nonEmpty
    then depsOn.mkString(".dependsOn(", ", ", ")")
    else ""
  end dependsOn

end SetupConfig

object SetupConfig:

  def apply(
      projectName: String,
      packageConfRelPath: os.RelPath,
      versionConfig: VersionConfig,
      reposConfig: ReposConfig,
      sbtDockerSettings: String
  ): SetupConfig = new SetupConfig(
    projectName,
    apiProjectConf = ApiProjectConf.init(projectName, projectDir(projectName, os.pwd) / packageConfRelPath),
    versionConfig = versionConfig,
    reposConfig = reposConfig,
    sbtDockerSettings = sbtDockerSettings
  )

  def defaultConfig(projectName: String): SetupConfig = SetupConfig(
    projectName,
    apiProjectConf = ApiProjectConf.initDummy(projectName)
  )

  def projectDir(projectName: String, baseDir: os.Path): os.Path =
    if baseDir.toString.endsWith(projectName) then baseDir else baseDir / projectName

  lazy val modules: Seq[ModuleConfig] = Seq(
    bpmnModule,
    apiModule,
    dmnModule,
    simulationModule,
    workerModule,
    helperModule
  )

  lazy val bpmnModule = ModuleConfig(
    "bpmn",
    level = 2,
    generateSubModule = true,
    hasProjectDependencies = true
  )
  lazy val apiModule = ModuleConfig(
    "api",
    level = 3
  )
  lazy val dmnModule = ModuleConfig(
    "dmn",
    level = 3,
    doPublish = false
  )
  lazy val simulationModule = ModuleConfig(
    "simulation",
    level = 3,
    hasMain = false,
    hasTest = true,
    doPublish = false
  )
  lazy val workerModule = ModuleConfig(
    "worker",
    level = 3,
    hasTest = true,
    sbtSettings = Seq("dockerSettings"),
    sbtPlugins = Seq("DockerPlugin", "JavaAppPackaging"),
    sbtDependencies = Seq("springBoot", "jaxbXml"),
    hasProjectDependencies = true,
    projectDependenciesTestOnly = true
  )
  lazy val helperModule = ModuleConfig(
    "helper",
    level = 4
  )

end SetupConfig

case class ModuleConfig(
    name: String,
    level: Int,
    hasMain: Boolean = true,
    hasTest: Boolean = false,
    generateSubModule: Boolean = false,
    doPublish: Boolean = true,
    sbtSettings: Seq[String] = Seq.empty,
    sbtPlugins: Seq[String] = Seq.empty,
    sbtDependencies: Seq[String] = Seq.empty,
    hasProjectDependencies: Boolean = false,
    projectDependenciesTestOnly: Boolean = false
):
  lazy val nameWithLevel: String =
    s"${"%02d".format(level)}-$name"

  def packagePath(
      projectPath: os.RelPath,
      mainOrTest: String = "main",
      subProject: Option[String] = None
  ) =
    val subPackage = subProject.toSeq
    val subModule = if generateSubModule then subPackage else Seq.empty
    os.rel / nameWithLevel /
      subModule / "src" / mainOrTest / "scala" /
      projectPath / name / subPackage
  end packagePath
end ModuleConfig

case class VersionConfig(
    scalaVersion: String = BuildInfo.scalaVersion,
    camundalaVersion: String = BuildInfo.version,
    customerCamundalaVersion: String = "0.1.0-SNAPSHOT",
    sbtVersion: String = BuildInfo.sbtVersion,
    springBootVersion: String = "3.1.2",
    jaxbXmlVersion: String = "2.3.1",
    munitVersion: String = "0.7.29",
    otherVersions: Map[String, String] = Map.empty
)
