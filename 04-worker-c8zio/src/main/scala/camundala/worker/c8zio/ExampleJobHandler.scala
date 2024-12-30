package camundala.worker.c8zio

import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.{JobClient, JobHandler}

class ExampleJobHandler extends JobHandler:

  def handle(client: JobClient, job: ActivatedJob): Unit =
    println(s"Handling Job: ${job}")
    client.newCompleteCommand(job.getKey).send().join()


end ExampleJobHandler
