package camundala.examples.demos

import camundala.simulation.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import DecisionResultTypes.*
import camundala.simulation.custom.CustomSimulation

import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *DecisionResultTypesSimulation

class DecisionResultTypesSimulation extends CustomSimulation:
  import TestDomain.*

  lazy val simulation = simulate {
    scenario(singleEntryDMN)
    scenario(singleResultDMN)
    scenario(collectEntriesDMN)
    scenario(resultListDMN)
    scenario(collectEntriesDMNEmptySeq)
    scenario(resultListDMNEmptySeq)
    /*  TestOverrides Example*/
    scenario(collectEntriesOverride)
    scenario(resultListOverride)
    /* bad cases
    scenario(singleResultDMNBadOutput)
    scenario(resultListDMNBadOutput)*/

  }

  override implicit def config =
    super.config.withPort(8033)

  private lazy val collectEntriesOverrideDMN = collectEntriesDMN
  private lazy val collectEntriesOverride =
    collectEntriesOverrideDMN
      .hasSize(2)
      .contains(1)
      .contains(2)

  private lazy val resultListOverrideDMN = resultListDMN
  private lazy val resultListOverride =
    resultListOverrideDMN
      .hasSize(2)
      .contains(ManyOutResult(1, "🤩"))
      .contains(ManyOutResult(2, "😂"))
