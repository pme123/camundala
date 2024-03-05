package camundala.helper.setup

import camundala.BuildInfo
import camundala.api.ApiConfig
import camundala.api.docs.ApiProjectConf
import camundala.helper.util.ReposConfig
import os.{Path, RelPath}

case class SetupConfig(
    projectName: String,
    baseDir: os.Path = os.pwd,
    modules: Seq[ModuleConfig] = SetupConfig.modules,
    subProjects: Seq[String] = Seq.empty,
    apiProjectConf: ApiProjectConf =  SetupConfig.apiProjectConf,
    versionConfig: VersionConfig = VersionConfig(),
    repoConfig: ReposConfig = ReposConfig.dummyRepos,
    sbtDockerSettings: String = ""
):
  lazy val projectDir: Path = if baseDir.toString.endsWith(projectName) then baseDir else baseDir / projectName
  lazy val projectPackage: String = projectName.split("-").mkString(".")
  lazy val projectPath: RelPath = os.rel / projectName.split("-")
  lazy val sbtProjectDir: Path = projectDir / "project"

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
  
  def defaultConfig(projectName: String) = SetupConfig(
    projectName
  )
  
  lazy val modules = Seq(
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
  lazy val apiProjectConf =
    ApiProjectConf(ApiConfig("myCompany").projectConfPath)
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
