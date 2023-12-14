package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.SimulationTestOverridesExample.{SimpleObject, simulationProcess}
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *SimulationTestOverridesExampleSimulation
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
