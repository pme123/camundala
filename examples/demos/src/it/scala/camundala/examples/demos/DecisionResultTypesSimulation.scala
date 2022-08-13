package camundala.examples.demos

import camundala.simulation.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import DecisionResultTypes.*
import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *DecisionResultTypesSimulation
class DecisionResultTypesSimulation extends SimulationDsl:

  override implicit def config: SimulationConfig =
    super.config.withPort(8033)

  import TestDomain.*
  simulate {
    scenario(singleEntryDMN)
    scenario(singleResultDMN)
    scenario(collectEntriesDMN)
    scenario(resultListDMN)
    scenario(collectEntriesDMNEmptySeq)
    scenario(resultListDMNEmptySeq)
    /*   TestOverrides Example */
    scenario(collectEntriesOverrideDMN)
    scenario(resultListOverrideDMN)
    /* bad cases */
    scenario(singleResultDMNBadOutput)
    scenario(resultListDMNBadOutput)

  }

  private lazy val collectEntriesOverrideDMN =
    collectEntriesDMN
      .hasSize(2)
      .contains(1)
      .contains(2)

  private lazy val resultListOverrideDMN =
    resultListDMN
      .hasSize(2)
      .contains(ManyOutResult(1, "🤩"))
      .contains(ManyOutResult(2, "😂"))
