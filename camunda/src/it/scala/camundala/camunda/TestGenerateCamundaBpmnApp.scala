package camundala.camunda

import camundala.bpmn.*
import camundala.domain.*
import camundala.camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TestGenerateCamundaBpmnApp extends GenerateCamundaBpmn, App:

  val projectPath = pwd / "camunda" / "src" / "it"
  override lazy val generatedPath: Path = projectPath / "resources"

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

