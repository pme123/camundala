package pme123.camundala.cli

case class ProjectInfo(name: String, org: String, version: String, sourceUrl: String, license: String = "MIT")

object ProjectInfo {
  val nameLabel = "Project: "
  val orgLabel = "Organization: "
  val versionLabel = "Version: "
  val sourceUrlLabel = "Source Code: "
  val licenseLabel = "License: "

}
