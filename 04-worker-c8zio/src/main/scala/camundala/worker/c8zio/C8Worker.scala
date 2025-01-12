package camundala.worker.c8zio

import camundala.worker.JobWorker
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.{JobClient, JobHandler}

trait C8Worker extends JobWorker, JobHandler:
    def handle(client: JobClient, job: ActivatedJob): Unit =
        println(s"Handling Job: ${job}")
        client.newCompleteCommand(job.getKey).send().join()

end C8Worker
