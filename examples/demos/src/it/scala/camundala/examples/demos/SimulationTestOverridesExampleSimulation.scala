package camundala.examples.demos

import camundala.examples.demos.SimulationTestOverridesExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *SimulationTestOverridesExampleSimulation
class SimulationTestOverridesExampleSimulation extends DemosSimulation:

  simulate(
    simulationProcess
      // simple and collection
      .exists("simpleValue")
      .exists("collectionValue")
      .notExists("SimpleValue")
      .notExists("CollectionValue")
      .isEquals("simpleValue", "hello")
      .isEquals("collectionValue", Seq("hello", "bye"))
      .isEquals("objectValue", SimpleObject())
      .isEquals(
        "objectCollectionValue",
        Seq(SimpleObject(), SimpleObject("tschau", true))
      )
      // only collection
      .hasSize("collectionValue", 2)
      .hasSize("objectCollectionValue", 2)
      .contains("collectionValue", "bye")
      .contains("objectCollectionValue", SimpleObject())
  )
