#!/usr/bin/env -S scala shebang

//> using toolkit 0.5.0
//> using dep io.github.pme123::camundala-helper:1.30.28

import camundala.helper.*

@main
def release(version: String): Unit =
  println(s"Publishing camundala: $version")
  ChangeLogUpdater.verifyChangelog(version)

  val releaseVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$"""
  if !version.matches(releaseVersion) then
    throw new IllegalArgumentException(
      "Your Version has not the expected format (2.1.2(-SNAPSHOT))"
    )

  replaceVersion(version)

  val isSnapshot = version.contains("-")
  runInConsole("sbt", "-J-Xmx3G", "docs/mdoc", "docs/laikaSite")

  if !isSnapshot then
    runInConsole("sbt", "-J-Xmx3G", "publishSigned")
    runInConsole("git", "fetch", "--all")
    runInConsole("git", "commit", "-a", "-m", s"Released Version $version")
    runInConsole("git", "tag", "-a", s"v$version", "-m", s"Version $version")
    runInConsole("git", "checkout", "master")
    runInConsole("git", "merge", "develop")
    runInConsole("git", "push", "--tags")
    runInConsole("git", "checkout", "develop")
    val pattern = """^(\d+)\.(\d+)\.(\d+)$""".r

    val newVersion = version match
      case pattern(major, minor, _) =>
        s"$major.${minor.toInt + 1}.0-SNAPSHOT"
    replaceVersion(newVersion)
    runInConsole("git", "commit", "-a", "-m", s"Init new Version $newVersion")
    runInConsole("git", "push", "--all")
    println(s"Published Version: $version")
  else
    runInConsole("sbt", "-J-Xmx3G", "publishLocal")
  end if
end release

private def replaceVersion(newVersion: String) =
  val versionsPath = os.pwd / "version"
  os.write.over(versionsPath, newVersion)

private def runInConsole(proc: String*) =
  println(proc.mkString(" "))
  val result = os.proc(proc).call()
  println(result.out.text())
