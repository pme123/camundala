package camundala.helper.util

import camundala.BuildInfo
import camundala.api.{ApiProjectConfig, DocProjectConfig, defaultProjectConfigPath}
import os.RelPath

case class DevConfig(
    // project configuration taken from the PROJECT.conf
    apiProjectConfig: ApiProjectConfig,
    // additional sbt configuration for sbt generation
    sbtConfig: SbtConfig = SbtConfig(),
    // versions used for generators
    versionConfig: CompanyVersionConfig = CompanyVersionConfig(),
    // If you have a Postman account, add the config here (used for ./helper.scala deploy..)
    postmanConfig: Option[PostmanConfig] = None,
    // Adjust the DockerConfig (used for ./helper.scala deploy../ docker..)
    dockerConfig: DockerConfig = DockerConfig(),
    // If you have a webdav server to publish the docs, add the config here (used in ./helper.scala publish..)
    publishConfig: Option[PublishConfig] = None,
    // general project structure -  do not change if possible -
    modules: Seq[ModuleConfig] = DevConfig.modules
):
  lazy val baseDir: os.Path               = os.pwd
  lazy val projectName: String            = apiProjectConfig.projectName
  lazy val companyName: String            = apiProjectConfig.companyName
  lazy val companyClassName: String       = companyName.head.toUpper + companyName.tail
  lazy val projectShortName: String       = projectName.split("-").tail.mkString("-")
  lazy val projectClassNames: Seq[String] = projectName.split("-").map(n => n.head.toUpper + n.tail)
  lazy val projectShortClassName: String  = projectClassNames.last
  lazy val projectClassName: String       = projectClassNames.mkString

  lazy val projectDir: os.Path      = DevConfig.projectDir(projectName, baseDir)
  // subProjects to optimize compilation time - use only for big projects
  lazy val subProjects: Seq[String] = apiProjectConfig.subProjects

  lazy val projectPackage: String  = projectName.split("-").mkString(".")
  lazy val projectPath: os.RelPath = os.rel / projectName.split("-")
  lazy val sbtProjectDir: os.Path  = projectDir / "project"

  def dependsOn(level: Int): String =
    val aboveLevel =
      modules
        .sortBy(_.level)
        .span(_.level < level)
        ._1.lastOption
        .map(_.level)
        .getOrElse(0)

    val depsOn = modules
      .filter(_.level == aboveLevel)
      .map(_.name)
    if depsOn.nonEmpty
    then depsOn.mkString(".dependsOn(", ", ", ")")
    else ""
  end dependsOn

  def withVersionConfig(versionConfig: CompanyVersionConfig): DevConfig =
    copy(versionConfig = versionConfig)
  def withSbtConfig(sbtConfig: SbtConfig): DevConfig                    =
    copy(sbtConfig = sbtConfig)
  def withPostmanConfig(postmanConfig: PostmanConfig): DevConfig        =
    copy(postmanConfig = Some(postmanConfig))
  def withDockerConfig(dockerConfig: DockerConfig): DevConfig           =
    copy(dockerConfig = dockerConfig)
  def withPublishConfig(publishConfig: PublishConfig): DevConfig        =
    copy(publishConfig = Some(publishConfig))

end DevConfig

object DevConfig:

  def init(packageConfPath: os.Path = os.pwd / defaultProjectConfigPath): DevConfig = new DevConfig(
    apiProjectConfig = ApiProjectConfig(packageConfPath)
  )

  def initCompany(
      projectName: String,
      packageConfPath: os.Path = os.pwd / defaultProjectConfigPath
  ): DevConfig =
    new DevConfig(
      apiProjectConfig =
        ApiProjectConfig.init(projectName, packageConfPath)
    )

  def configForCompany(projectName: String): DevConfig = DevConfig(
    apiProjectConfig = ApiProjectConfig(projectName, "0.1.0-SNAPSHOT")
  )

  def projectDir(projectName: String, baseDir: os.Path): os.Path =
    println(s"baseDir: $baseDir - projectName: $projectName -${
        if baseDir.toString.endsWith(projectName) then baseDir else baseDir / projectName
      }")
    if baseDir.toString.toLowerCase.endsWith(projectName.toLowerCase) then baseDir
    else baseDir / projectName
  end projectDir

  import ModuleConfig.*

  lazy val modules: Seq[ModuleConfig] = Seq(
    domainModule,
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
    val subPackage       = subProject.toSeq
    val subModule        = if generateSubModule then subPackage else Seq.empty
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
  lazy val domainModule     = ModuleConfig(
    "domain",
    level = 1,
    testType = TestType.MUnit,
    generateSubModule = true,
    hasProjectDependencies = true
  )
  lazy val apiModule        = ModuleConfig(
    "api",
    level = 3
  )
  lazy val dmnModule        = ModuleConfig(
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
  lazy val workerModule     = ModuleConfig(
    "worker",
    level = 3,
    testType = TestType.ZIO,
    sbtSettings = Seq("dockerSettings"),
    sbtPlugins = Seq("DockerPlugin", "JavaAppPackaging"),
    sbtDependencies = Seq(
      """"ch.qos.logback" % "logback-classic" % logbackVersion % Runtime""",
      """"jakarta.xml.bind" % "jakarta.xml.bind-api" % jaxbApiVersion"""
    ),
    hasProjectDependencies = true
  )
  lazy val helperModule     = ModuleConfig(
    "helper",
    level = 4
  )

end ModuleConfig

enum TestType:
  case MUnit, Simulation, ZIO, None

case class CompanyVersionConfig(
    scalaVersion: String = BuildInfo.scalaVersion,
    camundalaVersion: String = BuildInfo.version,
    companyCamundalaVersion: String = "0.1.0-SNAPSHOT",
    camundaVersion: String = BuildInfo.camundaVersion,
    sbtVersion: String = BuildInfo.sbtVersion,
    springBootVersion: String = BuildInfo.springBootVersion,
    jaxbXmlVersion: String = BuildInfo.jaxbApiVersion,
    munitVersion: String = BuildInfo.mUnitVersion,
    zioVersion: String = BuildInfo.zioVersion,
    logbackVersion: String = BuildInfo.logbackVersion,
    otherVersions: Map[String, String] = Map.empty
)
