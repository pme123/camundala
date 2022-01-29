package camundala.examples.demos

import camundala.bpmn.*
import camundala.camunda.*
import camundala.domain.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TestGenerateCamundaBpmnApp extends GenerateCamundaBpmn, App:

  val projectPath = pwd / "examples" / "demos"

  import TestDomain.*
  run(
    Bpmn(
      withIdPath / "generate-test.bpmn",
      CamundalaGenerateTestP.bpmn
        .withElements(
          CallProcessCA
            //.mapOut[ProcessOut, String](_.result, _.successStr)
            .mapIn(ProcessIn())(_.someObj.isOk, _.putTag)
        )
    )
  )

end TestGenerateCamundaBpmnApp

