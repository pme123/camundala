package camundala.examples.demos
package dmn

import camundala.bpmn.*
import camundala.dmn.*
import bpmn.*

import java.time.LocalDateTime

object ProjectDmnTester
    extends DmnTesterConfigCreator,
      DmnTesterStarter,
      App:

  private lazy val localDmnConfigPath: os.Path =
    os.pwd / "05-examples" / "demos" / "03-dmn" / "src" / "main" / "resources" / "dmnConfigs"

  private lazy val localDmnPath = os.pwd / "05-examples" / "demos" / "04-c7-spring" / "src" / "main" / "resources"

  override def starterConfig: DmnTesterStarterConfig =
    DmnTesterStarterConfig(
      dmnPaths = Seq(localDmnPath),
      dmnConfigPaths = Seq(localDmnConfigPath)
    )

  startDmnTester()

  createDmnConfigs(
    DecisionResultTypes.singleEntryDMN.testUnit
      .dmnPath("DecisionResultTypes")
      .testValues(_.letter, "A", "B", "C"),
    DecisionResultTypes.collectEntriesDMN.testUnit
      .dmnPath("DecisionResultTypes")
      .testValues(_.letter, "A", "B", "C"),
    DecisionResultTypes.singleResultDMN.testUnit
      .dmnPath("DecisionResultTypes"),
    DecisionResultTypes.resultListDMN.testUnit
      .dmnPath("DecisionResultTypes")
      .testValues(_.letter, "A", "B", "C"),
    DateExample.DateExampleDMN.testUnit
      .testValues(
        _.inDate,
        Seq("2012-12-12T12:12:12", "2012-12-12T12:12:11", "2012-12-12T12:12:13")
          .map(LocalDateTime.parse): _*
      ),
    VariablesExample.VariablesExampleDMN
      .testUnit
      .testValues(
        _.letters,
        "A_dynamic_2", "B", "C"
      )
  )

end ProjectDmnTester
