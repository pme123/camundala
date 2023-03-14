package camundala.examples.demos.dmn

import camundala.bpmn.*
import camundala.dmn.*
import camundala.domain.*
import camundala.examples.demos.DateExample.*
import camundala.examples.demos.DecisionResultTypes.*
import camundala.examples.demos.VariablesExample.VariablesExampleDMN

import java.time.LocalDateTime

object DemosDmnTesterConfigCreator
    extends DmnTesterConfigCreator,
      DmnConfigWriter,
      DmnTesterStarter,
      App:

  override protected val projectBasePath: Path =
    pwd / "examples" / "demos"

  override val starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig(
  )

  startDmnTester()

  createDmnConfigs(
    singleEntryDMN.testUnit.inTestMode
      .dmnPath("DecisionResultTypes")
      .testValues("letter", "A", "B", "C"),
    collectEntriesDMN.testUnit.inTestMode
      .dmnPath("DecisionResultTypes")
      .testValues("letter", "A", "B", "C"),
    singleResultDMN.testUnit.inTestMode
      .dmnPath("DecisionResultTypes"),
    resultListDMN.testUnit.inTestMode
      .dmnPath("DecisionResultTypes")
      .testValues("letter", "A", "B", "C"),
    DateExampleDMN.testUnit
      .inTestMode
      .testValues(
        "inDate",
        Seq("2012-12-12T12:12:12", "2012-12-12T12:12:11", "2012-12-12T12:12:13")
          .map(LocalDateTime.parse): _*
      ),
    VariablesExampleDMN
      .testUnit
      .testValues(
        "letters",
        "A_dynamic_2", "B", "C"
      )
  )

end DemosDmnTesterConfigCreator
