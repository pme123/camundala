package camundala.examples.demos

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.demos.DecisionResultTypes.*
import camundala.examples.demos.EnumExample.enumExample

// exampleDemos/run
object DemosApiCreator extends DefaultApiCreator:

  val projectName = "demos-example"

  protected val title = "Demos Example Process API"

  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(os.pwd / "05-examples" / "demos")
      .withDocProjectUrl(project => s"https://webstor.ch/camundala/myCompany/$project")
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
    api(
      demoProcess
        .withDiagramName("mapping-example")
    )(
      singleEntryDMN.withDiagramName("DecisionResultTypes"),
      collectEntriesDMN.withDiagramName("DecisionResultTypes"),
      singleResultDMN.withDiagramName("DecisionResultTypes"),
      resultListDMN.withDiagramName("DecisionResultTypes")
    ),
    enumExample,
    DateExample.DateExampleDMN,
    VariablesExample.VariablesExampleDMN,
    SimulationTestOverridesExample.simulationProcess,
    group("SignalMessageExample")(
      SignalMessageExample.signalExample,
      SignalMessageExample.messageExample,
      SignalMessageExample.signalIntermediateExample,
      SignalMessageExample.messageIntermediateExample
    ),
    api(TimerExample.timerProcess)(
      TimerExample.timer
    )
  )
