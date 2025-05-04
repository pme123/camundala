package camundala.examples.demos.newWorker

import camundala.domain.GeneralVariables
import camundala.worker.EngineRunContext
import camundala.worker.c7zio.C7Context

// Create a mock EngineRunContext
private[newWorker] given EngineRunContext = EngineRunContext(
  new C7Context {},
  GeneralVariables(servicesMocked = true)
)
