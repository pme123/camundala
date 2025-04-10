package camundala.examples.demos.newWorker

object TestWorker2App extends CompanyWorkerApp:

  workers(
    ExampleJob2Worker()
  )
  dependencies(
    TestWorkerApp
  )
end TestWorker2App
