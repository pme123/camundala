package camundala.examples.demos

import camundala.api.*
import camundala.bpmn.*
import DecisionResultTypes.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object DemosApiCreator extends DefaultApiCreator:

  val projectName = "demos-example"

  protected val title = "Demos Example Process API"

  protected val version = "1.0"

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "demos")
      .withPort(8034)

  document {
    api(demoProcess)(
      singleEntryDMN,
      collectEntriesDMN,
      singleResultDMN,
      resultListDMN
    )
  }
