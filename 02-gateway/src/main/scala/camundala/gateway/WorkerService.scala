package camundala.gateway

import sttp.tapir.Schema.annotations.description

trait WorkerService:
  @description("Starts a worker synchronously")
  def startWorker[In <: Product](
      @description("Worker definition ID") workerDefId: String, 
      @description("Input variables") in: In
  ): ProcessInfo

  @description("Registers a worker for a specific topic")
  def registerTopic(
      @description("Topic name to subscribe to") topicName: String
  ): Unit