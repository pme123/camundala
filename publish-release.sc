import mainargs._

import $ivy.`io.github.pme123:camundala-helper_3:1.15.13 compat`, camundala.helper._

/** <pre> Creates a new Release for the client and publishes to
  * bpf-generic-release:
  *
  * amm ./publish-release.sc <VERSION>
  *
  * # Example SNAPSHOT (only publish to bpf-generic-dev) amm
  * ./publish-release.sc 0.2.5-SNAPSHOT
  *
  * # Example (publish to bpf-generic-release and GIT Tagging and increasing
  * Version to next minor Version) amm ./publish-release.sc 0.2.5
  */

@arg(doc = "> Creates a new Release and publishes to Maven Central")
@main
def release(version: String): Unit = {
  println(s"Publishing camundala: $version")

  val releaseVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$"""
  if (!version.matches(releaseVersion))
    throw new IllegalArgumentException(
      "Your Version has not the expected format (2.1.2(-SNAPSHOT))"
    )

  replaceVersion(version)

  val isSnapshot = version.contains("-")
  os.proc("sbt", "-J-Xmx3G", "laikaSite").call()

  if (!isSnapshot) {
    ChangeLogUpdater.verifyChangelog(version)
    os.proc("sbt", "-J-Xmx3G", "publishSigned").call()
    os.proc("git", "fetch", "--all").call()
    os.proc("git", "commit", "-a", "-m", s"Released Version $version").call()
    os.proc("git", "tag", "-a", s"v$version", "-m", s"Version $version").call()
    os.proc("git", "checkout", "master").call()
    os.proc("git", "merge", "develop").call()
    os.proc("git", "push", "--tags").call()
    os.proc("git", "checkout", "develop").call()
    val pattern = """^(\d+)\.(\d+)\.(\d+)$""".r

    val newVersion = version match {
      case pattern(major, minor, _) =>
        s"$major.${minor.toInt + 1}.0-SNAPSHOT"
    }
    replaceVersion(newVersion)
    os.proc("git", "commit", "-a", "-m", s"Init new Version $newVersion").call()
    os.proc("git", "push", "--all").call()
    println(s"Published Version: $version")
  } else
    os.proc("sbt", "-J-Xmx3G", "publishLocal").call()

}
private def replaceVersion(newVersion: String) = {
  val versionsPath = os.pwd / "version"
  os.write.over(versionsPath, newVersion)
}
