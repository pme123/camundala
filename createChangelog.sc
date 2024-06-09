import mainargs.*

import $ivy.`io.github.pme123:camundala-helper_3:1.15.13 compat`, camundala.helper.*

/** <pre> Creates a new Release for the client and publishes to bpf-generic-release:
  *
  * amm ./publish-release.sc <VERSION>
  *
  * # Example SNAPSHOT (only publish to bpf-generic-dev) amm ./publish-release.sc 0.2.5-SNAPSHOT
  *
  * # Example (publish to bpf-generic-release and GIT Tagging and increasing Version to next minor
  * Version) amm ./publish-release.sc 0.2.5
  */

@arg(doc = "> Creates the Changelog for a certain version")
@main
def create(version: String): Unit =
  println(s"Publishing camundala: $version")

  val releaseVersion = """^(\d+)\.(\d+)\.(\d+)(-.*)?$"""
  if !version.matches(releaseVersion) then
    throw new IllegalArgumentException(
      "Your Version has not the expected format (2.1.2(-SNAPSHOT))"
    )

  replaceVersion(version)
  ChangeLogUpdater.verifyChangelog(version)
end create
private def replaceVersion(newVersion: String) =
  val versionsPath = os.pwd / "version"
  os.write.over(versionsPath, newVersion)
