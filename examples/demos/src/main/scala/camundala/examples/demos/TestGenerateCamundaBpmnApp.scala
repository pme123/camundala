package camundala.examples.demos

import camundala.bpmn.*
import camundala.camunda.*

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
            // simple Example
            .mapOut[ProcessOut, String](_.result, _.successStr)
            // path example
            .mapIn[ProcessIn, String](_.someObj.isOk, _.putTag)
            // object example
            .mapIn[ProcessIn, ValueWrapper](_.success, _.success)
            // change type example
         //TODO   .mapOut[ProcessOut, Boolean, String](_.success.success, _.isBoolean)
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
