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
            .mapIn[ProcessIn, String](_.someObj.isOk, _.putTag)
            .mapOut[ProcessOut, String](_.result, _.successStr)
            // option Example
            .mapIn[ProcessIn, Option[String]](_.optionExample, _.someOption)
            .mapOut[ProcessOut, Option[String]](_.someOption, _.optionResult)
            // list Example
            .mapIn[ProcessIn, Seq[String]](_.listExample, _.someList)
            .mapOut[ProcessOut, Seq[String]](_.someList, _.listResult)
        )
    )
  )

end TestGenerateCamundaBpmnApp
