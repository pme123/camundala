package camundala.helper.dev.company

import camundala.helper.dev.update.createIfNotExists

import java.time.LocalDate

case class CompanyDocsGenerator(companyCamundala: os.Path):
  private lazy val companyProjectName = companyCamundala.last
  private lazy val docsBase = companyCamundala / s"00-docs"
  private lazy val docsSrc = docsBase / "src" / "docs"

  lazy val generate: Unit =
    println("Generate Company Docs")
    // generate docs
    directory("dependencies", "Dependencies", isVersioned = true)
    directory("helium", "Helium", isVersioned = false)
    contact
    instructions
    favicon
    pattern
    statistics
    style
    config
    versions("VERSIONS")
    versions("VERSIONS_PREVIOUS")
  end generate

  private lazy val contact =
    createIfNotExists(
      docsSrc / "contact.md",
      s"""|## Contact
          |If you have questions, spot a bug or you miss something, please let us know🤓.
          |
          |- _Business_
          |    - [Peter Blank](mailto:peter.blank@todo.ch)
          |- _Technical_
          |    - [Maya Blue](mailto:maya.blue@todo.ch)
          |""".stripMargin
    )
  private lazy val instructions =
    createIfNotExists(
      docsSrc / "instructions.md",
      s"""|## Create a Release
          |This is a semi-automatic process. This should be done either to prepare a Release or after a Release.
          |
          |@:callout(info)
          |Be aware this requires a Postman Account and a collection, that runs the deployment.
          |
          |See [Setup Postman]($${camundala.docs}/company/postman.html)
          |@:@
          |Do the following steps:
          |
          |- Check out this project _${companyProjectName}_: `git clone https://YOUR_REPO/$companyProjectName.git`
          |- Configure the Release - edit _00-docs/CONFIG.conf_.
          |- Copy the old Versions from _00-docs/VERSIONS.conf_ to _00-docs/VERSIONS_PREVIOUS.conf_.
          |- Copy the Versions of the Release to _00-docs/VERSIONS.conf_  from Postman [Manage Deploy: YOUR_ENVIRONMENT](https://YOUR_POSTMAN_URL).
          |```
          |    // START VERSIONS
          |
          |    // Project
          |    myProjectVersion = "0.8.11" // new
          |    ...
          |    // END VERSIONS
          |    ```
          |- Prepare Docs Release: `00-docs/helper.scala prepareDocs`
          |
          |  @:callout(warning)
          |  Be aware that this overwrites `release.md`
          |  @:@
          |
          |- Manually adjust the Release Notes _release.md_.
          |    - You can check the result, using the _Sbt_ command _laikaPreview_ on [localhost](http://localhost:4242/index.html)
          |    - If you change the Versions you need to reload _SBT_.
          |- Publish Docs: `00-docs/helper.scala publishDocs`
          |- Check the result on [MyCompany Documentation](https://YOUR_DOCUMENTATION)
          |""".stripMargin
    )

  private lazy val pattern =
    createIfNotExists(
      docsSrc / "pattern.md",
      s"""|# Process Pattern
          |We try to establish Patterns for doing the same tasks.
          |This documentation lists them and gives you some examples.
          |
          |TODO: Describe the Patterns here that you want to establish.
          |""".stripMargin
    )
  private lazy val statistics =
    createIfNotExists(
      docsSrc / "statistics.md",
      s"""|# Process Statistics
          |
          |The Process Statistics you find new in Camunda Optimize.
          |
          |TODO - Create here a link to the Optimize Dashboard or add some statistics manually.
          |
          |<iframe id="optimizeFrame" src="https://TODO/" frameborder="0" style="width: 1000px; height: 700px; allowtransparency; overflow: scroll"></iframe>
          |""".stripMargin
    )
  private lazy val style =
    createIfNotExists(
      docsSrc / "style.css",
      s"""|.mermaid svg {
          |    height: 400px;
          |}
          |.colorLegend {
          |    margin-left: auto;
          |    margin-right: 40px;
          |    width: 400px;
          |}
          |""".stripMargin
    )

  private lazy val favicon =
    val faviconPath = docsSrc / "favicon.ico"
    if !os.exists(faviconPath) then
      os.write(faviconPath, (os.resource / "favicon.ico").toSource)

  private def directory(name: String, title: String, isVersioned: Boolean) =
    os.makeDir.all(docsSrc / name)
    createIfNotExists(
      docsSrc / name / "directory.md",
      s"""|${laikaVersioned(isVersioned)}
          |
          |${laikaTitle(title)}
          |
          |$laikaNavigationOrder
          |""".stripMargin
    )
  end directory

  private def laikaVersioned(isVersioned: Boolean) = s"laika.versioned = $isVersioned"
  private def laikaTitle(title: String) = s"laika.title = $title"
  private lazy val laikaNavigationOrder = s"laika.navigationOrder = [\n]"

  private lazy val config =
    createIfNotExists(
      docsBase / "CONFIG.conf",
      s"""|// year and month you want to release
          |release.tag = "${LocalDate.now().toString.take(7)}"
          |// a list with existing Releases on the web server
          |releases.older = []
          |// flag of this is for the release or just from the TST to see what is going on.
          |released = true
          |// this is the url of the release planing, e.g. Jira
          |jira.release.url = "https://yourReleasePage/versions/64209"
          |// who is responsible for the Release
          |release.responsible {
          |  name = "Peter Blank"
          |  date = "CHANGE to release date"
          |}
          |// what is the release about (abstract as markup)
          |release.notes = \"\"\"
          |- TODO: Describe the Release here
          |\"\"\"
          |""".stripMargin
    )
  private def versions(name: String) =
    createIfNotExists(
      docsBase / s"$name.conf",
      s"""|// START VERSIONS
          |
          |myProjectWorkerVersion = "1.0.0"
          |//..
          |
          |// END WOKRER
          |
          |myProjectVersion = "1.0.3"
          |//..
          |
          |// END VERSIONS
          |""".stripMargin)
end CompanyDocsGenerator
