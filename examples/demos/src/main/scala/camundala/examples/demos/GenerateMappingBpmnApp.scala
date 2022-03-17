package camundala.examples.demos

import camundala.bpmn.*
import camundala.camunda.*
import camundala.domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import MappingDomain.*

object GenerateMappingBpmnApp extends GenerateCamundaBpmn, App:

  val projectPath = pwd / "examples" / "demos"

  import TestDomain.*
  run(
    Bpmn(
      withIdPath / "mapping-example.bpmn",
      CamundalaMappingExample.bpmn(
        GetAddressCA
          .mapIn[MappingExampleIn, Int](_.clientId, _.customer)
        ,
        PrintDocumentCA,
      )
      // CamundalaAddressService.bpmn, // nothing to generate
      // CamundalaPrintService.bpmn // nothing to generate
    )
  )

end GenerateMappingBpmnApp
