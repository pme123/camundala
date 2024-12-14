package camundala.helper.dev.company

import camundala.helper.dev.update.createIfNotExists

case class CompanyDocsGenerator (companyCamundala: os.Path):
  private lazy val docs = companyCamundala / s"00-docs" / "src" / "docs"

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

  private lazy val contact =
    createIfNotExists(docs / "contact.md",
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
    createIfNotExists(docs / "instructions.md",
      s"""|## Create This Page
          |This is a semi-automatic process. This should be done either to prepare a Release or after a Release.
          |
          |Do the following steps:
          |
          |- TODO describe your process here
          |""".stripMargin
    )

  private lazy val pattern =
    createIfNotExists(docs / "pattern.md",
      s"""|# Process Pattern
          |We try to establish Patterns for doing the same tasks.
          |This documentation lists them and gives you some examples.
          |
          |TODO: Describe the Patterns here that you want to establish.
          |""".stripMargin
    )
  private lazy val statistics =
    createIfNotExists(docs / "statistics.md",
      s"""|# Process Statistics
          |
          |The Process Statistics you find new in Camunda Optimize.
          |
          |TODO - Create here a link to the Optimize Dashboard or add some statistics manually.
          |
          |<iframe id="optimizeFrame" src="https://TODO/" frameborder="0" style="width: 1000px; height: 700px; allowtransparency; overflow: scroll"></iframe>
          |""".stripMargin)
  private lazy val style =
    createIfNotExists(docs / "style.md",
      s"""|.mermaid svg {
          |    height: 400px;
          |}
          |.colorLegend {
          |    margin-left: auto;
          |    margin-right: 40px;
          |    width: 400px;
          |}
          |""".stripMargin)

  private lazy val favicon =
    val faviconPath = docs / "favicon.ico"
    if !os.exists(faviconPath) then
      os.write(faviconPath, (os.resource / "favicon.ico").toSource)

  private def directory(name: String, title: String, isVersioned: Boolean) =
    os.makeDir.all(docs / name)
    createIfNotExists(docs / name / "directory.md",
      s"""|${laikaVersioned(isVersioned)}
        |
        |${laikaTitle(title)}
        |
        |$laikaNavigationOrder
        |""".stripMargin
    )

  private def laikaVersioned(isVersioned: Boolean) = s"laika.versioned = $isVersioned"
  private def laikaTitle(title: String) = s"laika.title = $title"
  private lazy val laikaNavigationOrder = s"laika.navigationOrder = [\n]"
