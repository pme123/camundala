package camundala.helper.util

import camundala.BuildInfo
import camundala.api.ApiProjectConf
import camundala.helper.util.ReposConfig
import os.RelPath

case class DevConfig(
                      projectName: String,
                      baseDir: os.Path = os.pwd,
                      modules: Seq[ModuleConfig] = DevConfig.modules,
                      subProjects: Seq[String] = Seq.empty,
                      apiProjectConf: ApiProjectConf,
                      versionConfig: VersionConfig = VersionConfig(),
                      reposConfig: ReposConfig = ReposConfig.dummyRepos,
                      // path where the BPMNs are - must be relative to the project path
                      bpmnPath: os.RelPath = os.rel / "src" / "main" / "resources",
                      sbtDockerSettings: Option[String] = None
):
  lazy val companyName: String = apiProjectConf.org
  lazy val companyClassName: String = companyName.head.toUpper + companyName.tail
  lazy val projectShortName: String = projectName.split("-").tail.mkString("-")
  lazy val projectClassNames: Seq[String] = projectName.split("-").map(n => n.head.toUpper + n.tail)
  lazy val projectShortClassName: String = projectClassNames.last
  lazy val projectClassName: String = projectClassNames.mkString

  lazy val projectDir: os.Path = DevConfig.projectDir(projectName, baseDir)

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

end DevConfig

object DevConfig:

  def apply(
      projectName: String,
      subProjects: Seq[String],
      packageConfRelPath: os.RelPath,
      versionConfig: VersionConfig,
      reposConfig: ReposConfig,
      sbtDockerSettings: String,
      bpmnPath: os.RelPath
  ): DevConfig = new DevConfig(
    projectName,
    subProjects = subProjects,
    apiProjectConf =
      ApiProjectConf.init(projectName, projectDir(projectName, os.pwd) / packageConfRelPath),
    versionConfig = versionConfig,
    reposConfig = reposConfig,
    sbtDockerSettings = Some(sbtDockerSettings),
    bpmnPath = bpmnPath
  )

  def defaultConfig(projectName: String): DevConfig = DevConfig(
    projectName,
    apiProjectConf = ApiProjectConf.initDummy(projectName)
  )

  def projectDir(projectName: String, baseDir: os.Path): os.Path =
    if baseDir.toString.endsWith(projectName) then baseDir else baseDir / projectName

  import ModuleConfig.*

  lazy val modules: Seq[ModuleConfig] = Seq(
    bpmnModule,
    apiModule,
    dmnModule,
    simulationModule,
    workerModule,
    helperModule
  )

end DevConfig

case class ModuleConfig(
    name: String,
    level: Int,
    testType: TestType = TestType.None,
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
      subProject: Option[String] = None,
      isSourceDir: Boolean = true
  ): RelPath =
    val subPackage = subProject.toSeq
    val subModule = if generateSubModule then subPackage else Seq.empty
    val sourceOrResource =
      if isSourceDir
      then os.rel / "scala" / projectPath / name / subPackage
      else os.rel / "resources"

    os.rel / nameWithLevel /
      subModule / "src" / mainOrTest / sourceOrResource
  end packagePath

  def emptyExportsFile(projPackage: String): String =
    s"""package $projPackage.$name
       |
       |// put here your exports - dummy file if you don't have any classes
       |""".stripMargin

end ModuleConfig

object ModuleConfig:
  lazy val bpmnModule = ModuleConfig(
    "bpmn",
    level = 2,
    testType = TestType.MUnit,
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
    testType = TestType.Simulation,
    doPublish = false
  )
  lazy val workerModule = ModuleConfig(
    "worker",
    level = 3,
    testType = TestType.MUnit,
    sbtSettings = Seq("dockerSettings"),
    sbtPlugins = Seq("DockerPlugin", "JavaAppPackaging"),
    hasProjectDependencies = true
  )
  lazy val helperModule = ModuleConfig(
    "helper",
    level = 4
  )

end ModuleConfig

enum TestType:
  case MUnit, Simulation, None

case class VersionConfig(
                          scalaVersion: String = BuildInfo.scalaVersion,
                          camundalaVersion: String = BuildInfo.version,
                          companyCamundalaVersion: String = "0.1.0-SNAPSHOT",
                          camundaVersion: String = BuildInfo.camundaVersion,
                          sbtVersion: String = BuildInfo.sbtVersion,
                          springBootVersion: String = BuildInfo.springBootVersion,
                          jaxbXmlVersion: String = BuildInfo.jaxbApiVersion,
                          munitVersion: String = BuildInfo.mUnitVersion,
                          otherVersions: Map[String, String] = Map.empty
)
