package camundala.helper.dev.publish

import camundala.api.ApiConfig
import camundala.helper.util.{DevConfig, Helpers, PublishConfig}

case class PublishHelper()(using
    devConfig: DevConfig,
    apiConfig: ApiConfig,
    publishConfig: Option[PublishConfig]
) extends Helpers:

  import PublishHelper.*

  def publish(version: String): Unit =
    println(s"Publishing BPF Package: $version")
    verifyVersion(version)
    verifySnapshots()
    verifyChangelog(version)
    pushDevelop()
    setApiVersion(version)
    replaceVersion(version)

    lazy val sbtProcs = Seq(
      "sbt",
      "publish"
    )
    lazy val sbtCreateDocs = "api/run"
    lazy val workerAppFile: os.Path =
      workDir / "03-worker" / "src" / "main" / "scala" /
        devConfig.projectPath / "worker" / "WorkerApp.scala"
    lazy val sbtDockerProcs =
      if os.exists(workerAppFile) then
        Seq(
          "worker / Docker / publish"
        )
      else
        Seq.empty

    println(s"workerAppFile ${os.exists(workerAppFile)}: $workerAppFile")
    println(s"SBT: ${(sbtProcs ++ sbtDockerProcs).mkString(" ")}")
    os.proc(sbtProcs ++ sbtDockerProcs :+ sbtCreateDocs).callOnConsole()

    val isSnapshot = version.contains("-")
    if !isSnapshot then
      publishToWebserver()
      git(version, replaceVersion)

    end if
  end publish

  private lazy val apiFile: os.Path =
    workDir / "03-api" / "src" / "main" / "scala" / devConfig.projectPath / "api" / "ApiProjectCreator.scala"

  private def setApiVersion(newVersion: String): Unit =
    if apiFile.toIO.exists() then
      val apiFileStr = os.read(apiFile)

      val pattern = """ version = "(\d+\..*\d+(-.+)?)""""
      val updatedFile =
        apiFileStr.replaceFirst(pattern, s""" version = "$newVersion"""")

      os.write.over(apiFile, updatedFile)

  private def replaceVersion(newVersion: String): Unit =
    PublishHelper.replaceVersion(newVersion, projectFile)
    PublishHelper.replaceVersion(newVersion, apiConfig.projectConfPath)
  end replaceVersion

  private def publishToWebserver(): Unit =
    // push it to Documentation Webserver
    publishConfig.foreach:
      ProjectWebDAV(devConfig.projectName, apiConfig, _).upload()

end PublishHelper

object PublishHelper extends Helpers:
  val projectFile: os.Path = workDir / "project" / "ProjectDef.scala"

  def verifyVersion(newVersion: String): Unit =
    val releaseVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$"""
    if !newVersion.matches(releaseVersion) then
      throw new IllegalArgumentException(
        "Your Version has not the expected format (2.1.2(-SNAPSHOT))"
      )
  end verifyVersion

  def verifyChangelog(newVersion: String): Unit =
    ChangeLogUpdater.verifyChangelog(
      newVersion,
      commitsAddress = _.replace(".git", "/commit/") // git
        .replace("ssh://git@", "https://") // ssh protocol
        .replace(":2222", "") // ssh port
    )

  def verifySnapshots(): Unit =
    hasSnapshots("Settings")
    hasSnapshots("ProjectDef")

  private def hasSnapshots(fileName: String): Unit =
    if os.read.lines(os.pwd / "project" / s"$fileName.scala")
        .exists(l => l.contains("-SNAPSHOT") && !l.contains("val version ="))
    then
      throw new IllegalArgumentException(
        s"There are SNAPSHOT dependencies in `project/$fileName.scala`"
      )

    // as projectUpdate for reference creation gets the newest changes from remote
  def pushDevelop(): Unit =
    os.proc("git", "push").callOnConsole()

  def replaceVersion(newVersion: String, versionFile: os.Path): Unit =
    val versionFileStr = os.read(versionFile)

    val regexPattern = """version = "(\d+\.\d+\.\d+(-.+)?)""""
    val updatedFile = versionFileStr
      .replaceAll(regexPattern, s"""version = "$newVersion"""") + "\n"

    os.write.over(versionFile, updatedFile)
  end replaceVersion

  def git(version: String, replaceVersion: String => Unit): Unit =
    val branch = "develop"
    os.proc("git", "fetch", "--all").callOnConsole()
    os.proc("git", "commit", "-a", "-m", s"Released Version $version")
      .callOnConsole()
    os.proc("git", "tag", "-a", "--no-sign", s"v$version", "-m", s"Version $version")
      .callOnConsole()
    os.proc("git", "checkout", "master").callOnConsole()
    os.proc("git", "merge", branch).callOnConsole()
    os.proc("git", "push", "--tags").callOnConsole()
    os.proc("git", "checkout", branch).callOnConsole()
    val Pattern = """^(\d+)\.(\d+)\.(\d+)$""".r

    val newVersion = version match
      case Pattern(major, minor, _) =>
        s"$major.${minor.toInt + 1}.0-SNAPSHOT"
    replaceVersion(newVersion)

    os.proc("git", "commit", "-a", "-m", s"Init new Version $newVersion")
      .callOnConsole()
    os.proc("git", "push", "--all").callOnConsole()
    println(s"Published Version: $version")
  end git

end PublishHelper
