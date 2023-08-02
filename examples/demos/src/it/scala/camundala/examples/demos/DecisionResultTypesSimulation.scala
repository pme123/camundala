package camundala.examples.demos

import camundala.simulation.*
import DecisionResultTypes.*

// exampleDemos/It/testOnly *DecisionResultTypesSimulation
class DecisionResultTypesSimulation extends DemosSimulation:

  simulate (
    singleEntryDMN,
    singleResultDMN,
    collectEntriesDMN,
    resultListDMN,
    collectEntriesDMNEmptySeq,
    resultListDMNEmptySeq,
    /*  TestOverrides Example*/
    collectEntriesOverride,
    resultListOverride,
    /* bad cases
    singleResultDMNBadOutput,
    resultListDMNBadOutput,*/
  )

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
