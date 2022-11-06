import $ivy.`com.lihaoyi::ammonite-ops:2.4.1 compat`, ammonite.ops._
import mainargs._

/**
 * <pre>
 * Creates a new Release for the client and publishes to bpf-generic-release:
 *
 * amm ./publish-release.sc <VERSION>
 *
 * # Example SNAPSHOT (only publish to bpf-generic-dev)
 * amm ./publish-release.sc 0.2.5-SNAPSHOT
 *
 * # Example (publish to bpf-generic-release and GIT Tagging and increasing Version to next minor Version)
 * amm ./publish-release.sc 0.2.5
 *
 */

private implicit val workDir: Path = {
  val wd = pwd
  println(s"Working Directory: $wd")
  wd
}

@arg(doc = "> Creates a new Release and publishes to Maven Central")
@main
def release(version: String): Unit = {
  println(s"Publishing camundala: $version")

  val releaseVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$"""
  if (!version.matches(releaseVersion))
    throw new IllegalArgumentException("Your Version has not the expected format (2.1.2(-SNAPSHOT))")

  replaceVersion(version)

  val isSnapshot = version.contains("-")
  %.sbt(
    "-J-Xmx3G",
    "publishSigned"
  )

  if (!isSnapshot) {

    %.git(
      "fetch",
      "--all"
    )
    %.git(
      "commit",
      "-a",
      "-m",
      s"Released Version $version"
    )
    %.git(
      "tag",
      "-a",
      version,
      "-m",
      s"Version $version"
    )
    %.git(
      "checkout",
      "master"
    )
    %.git(
      "merge",
      "develop"
    )
    %.git(
      "push",
      "--tags"
    )
    %.git(
      "checkout",
      "develop"
    )
    val pattern = """^(\d+)\.(\d+)\.(\d+)$""".r

    val newVersion = version match {
      case pattern(major, minor, _) =>
        s"$major.${minor.toInt + 1}.0-SNAPSHOT"
    }
    replaceVersion(newVersion)
    %.git(
      "commit",
      "-a",
      "-m",
      s"Init new Version $newVersion"
    )
    %.git(
      "push",
      "--all",
    )
    println("""Due to problems with the `"org.xerial.sbt" % "sbt-sonatype"` Plugin you have to release manually:
              |- https://s01.oss.sonatype.org/#stagingRepositories
              |  - login
              |  - check Staging Repository
              |  - hit _close_ Button (this will take some time)
              |  - hit _release_ Button (this will take some time)""".stripMargin)
    println(s"Published Version: $version")
  }
}
private def replaceVersion(newVersion: String) = {
  val versionsPath = pwd / "version"
  write.over(versionsPath, newVersion)
}