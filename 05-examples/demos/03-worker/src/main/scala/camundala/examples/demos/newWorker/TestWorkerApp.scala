package camundala.examples.demos.newWorker

import camundala.worker.{WorkerApp, WorkerClient}
import camundala.worker.c8zio.C8WorkerClient
  
trait CompanyWorkerApp extends WorkerApp:
  lazy val workerClients: Seq[WorkerClient[?]] = 
    Seq(C8WorkerClient)


object TestWorkerApp extends CompanyWorkerApp:
  workers(
    ExampleJobHandler
  )
  
  