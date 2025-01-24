package camundala.examples.demos.newWorker

import camundala.worker.{WorkerApp, WorkerRegistry}
import camundala.worker.c8zio.{C7NoAuthClient, C7WorkerRegistry, C8SaasClient, C8WorkerRegistry}
  
trait CompanyWorkerApp extends WorkerApp:
  lazy val workerRegistries: Seq[WorkerRegistry[?]] = 
    Seq(C8WorkerRegistry(C8SaasClient), C7WorkerRegistry(C7NoAuthClient))


object TestWorkerApp extends CompanyWorkerApp:
  workers(
    ExampleJobWorker
  )

  def asDependency = theWorkers
  
  