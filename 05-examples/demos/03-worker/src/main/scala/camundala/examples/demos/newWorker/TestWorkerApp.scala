package camundala.examples.demos.newWorker

import camundala.worker.{WorkerApp, WorkerRegistry}
import camundala.worker.c7zio.{C7NoAuthClient, C7WorkerRegistry, C8SaasClient, C8WorkerRegistry, OAuth2Client}
  
trait CompanyWorkerApp extends WorkerApp:
  lazy val workerRegistries: Seq[WorkerRegistry] = 
    Seq(C8WorkerRegistry(C8SaasClient), C7WorkerRegistry(CompanyOAuth2Client))


object TestWorkerApp extends CompanyWorkerApp:
  workers(
    ExampleJobWorker
  )


  