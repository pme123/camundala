package camundala.examples.demos.newWorker

import camundala.worker.{WorkerApp, WorkerRegistry}
import camundala.worker.c7zio.{C7NoAuthClient, C7WorkerRegistry, C8SaasClient, C8WorkerRegistry}


object TestWorker2App extends CompanyWorkerApp:
  workers(
    ExampleJob2Worker
  )
  dependencies(
    TestWorkerApp
  )

  
  