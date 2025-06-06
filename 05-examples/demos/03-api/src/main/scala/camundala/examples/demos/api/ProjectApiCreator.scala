package camundala.examples.demos
package api

import camundala.api.*
import bpmn.*

// exampleDemos/run
object ProjectApiCreator extends DefaultApiCreator:
  lazy val companyName = "MyCompany"
  
  protected val title = "Demos Example Process API"

  protected val version = "1.0"
  lazy val companyProjectVersion: String = "0.1.0"

  lazy val companyDescr: ExampleName = ""
  lazy val projectDescr: ExampleName = ""

  override protected val apiConfig: ApiConfig =
    ApiConfig("demoCompany")
      .withBasePath(os.pwd / "05-examples" / "demos")
      .withDocBaseUrl("https://webstor.ch/camundala/myCompany")
      .withPort(8034)

  document(
    api(
      DecisionResultTypes.demoProcess
        .withDiagramName("mapping-example")
    )(
      DecisionResultTypes.singleEntryDMN.withDiagramName("DecisionResultTypes"),
      DecisionResultTypes.collectEntriesDMN.withDiagramName("DecisionResultTypes"),
      DecisionResultTypes.singleResultDMN.withDiagramName("DecisionResultTypes"),
      DecisionResultTypes.resultListDMN.withDiagramName("DecisionResultTypes")
    ),
    GenericServiceExample.example,
    api(EnumExample.example)(
    EnumWorkerExample.example,
    DateExample.DateExampleDMN,
    VariablesExample.VariablesExampleDMN,
    ),
    SimulationTestOverridesExample.simulationProcess,
    group("SignalMessageExample")(
      SignalExample.signalExample,
      MessageForExample.messageExample,
      SignalExample.signalIntermediateExample,
      MessageForExample.messageIntermediateExample
    ),
    api(TimerExample.example)(
      TheTimer.example
    )
  )
end ProjectApiCreator
