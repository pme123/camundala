package camundala.examples.invoice.worker

import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}
import org.springframework.context.annotation.Configuration
import sttp.client3.{HttpClientFutureBackend, SimpleHttpClient, UriContext, basicRequest}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.given
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

lazy val servicePath =
  uri"https://swapi.dev/api/people/1"

lazy val client = SimpleHttpClient()
lazy val backend = HttpClientFutureBackend()

@Configuration
@ExternalTaskSubscription(value = "testGrv.sync")
class SyncTestWorker extends ExternalTaskHandler:
  
  def execute(
      externalTask: ExternalTask,
      externalTaskService: ExternalTaskService
  ): Unit =
    println(s" SYNC start $servicePath")
    try {
      val response = basicRequest
        .get(uri"$servicePath")
        .send(backend)
      println(s" SYNC request sent")
      val resp = Await.result(response, 10.seconds)
      println(s" ASYNC Status ${resp.statusText}")
      println(s" SYNC Result ${resp.body}")
      externalTaskService.complete(externalTask, Map("result" -> resp.body).asJava)
    } catch {
      case ex: Throwable =>
        ex.printStackTrace()
        externalTaskService.handleFailure(externalTask, ex.getMessage, "SYNC TEST", 0, 0)
    }
  end execute

end SyncTestWorker

@Configuration
@ExternalTaskSubscription(value = "testGrv.async")
class AsyncTestWorker extends ExternalTaskHandler:

  def execute(
      externalTask: ExternalTask,
      externalTaskService: ExternalTaskService
  ): Unit =
    println(s" ASYNC start $servicePath")
    try {
      val response = basicRequest
        .get(uri"$servicePath")
        .send(backend)
      println(s" ASYNC request sent")
      response
        .map { resp =>
          println(s" ASYNC Status ${resp.statusText}")
          println(s" ASYNC Result ${resp.body}")
          resp.body

        }
        .onComplete {
          case Success(value) =>
            externalTaskService.complete(externalTask, Map("result" -> value).asJava)
          case Failure(ex) =>
            externalTaskService.handleFailure(externalTask, ex.getMessage, "SYNC TEST", 0, 0)
        }

    } catch {
      case ex: Throwable =>
        ex.printStackTrace()
        externalTaskService.handleFailure(externalTask, ex.getMessage, "SYNC TEST", 0, 0)
    }
  end execute

end AsyncTestWorker
