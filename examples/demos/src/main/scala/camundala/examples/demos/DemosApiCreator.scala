package camundala.examples.demos

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.demos.DecisionResultTypes.*
import camundala.examples.demos.EnumExample.enumExample

object DemosApiCreator extends DefaultApiCreator:

  val projectName = "demos-example"

  protected val title = "Demos Example Process API"

  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "demos")
      .withDiagramDownloadPath(
        "src/main/resources"
      )
      .withPort(8034)

  document(
    singleEntryDMN
      .withDiagramName("DecisionResultTypes"),
    collectEntriesDMN
      .withDiagramName("DecisionResultTypes"),
    singleResultDMN
      .withDiagramName("DecisionResultTypes"),
    resultListDMN
      .withDiagramName("DecisionResultTypes"),
    api(demoProcess
      .withDiagramName("mapping-example")
    )(
      singleEntryDMN.withDiagramName("DecisionResultTypes"),
      collectEntriesDMN.withDiagramName("DecisionResultTypes"),
      singleResultDMN.withDiagramName("DecisionResultTypes"),
      resultListDMN.withDiagramName("DecisionResultTypes"),
    ),
    enumExample,
    DateExample.DateExampleDMN,
    VariablesExample.VariablesExampleDMN
  )
