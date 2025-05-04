package camundala.worker.c7zio

import camundala.worker.oauth.OAuthPasswordFlow
import camundala.worker.{Slf4JLogger, WorkerLogger}
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.*
import org.apache.hc.core5.http.*
import org.apache.hc.core5.http.protocol.HttpContext
import org.apache.hc.core5.util.Timeout
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.{ExponentialBackoffStrategy, ExponentialErrorBackoffStrategy}
import zio.ZIO
import zio.ZIO.*

import scala.concurrent.duration.*
import java.io.IOException
import java.util.Base64
import scala.jdk.CollectionConverters.*

trait C7Client:
  def client: ZIO[Any, Throwable, ExternalTaskClient]

object C7NoAuthClient extends C7Client:

  def client =
    ZIO.attempt:
      ExternalTaskClient.create()
        .baseUrl("http://localhost:8887/engine-rest")
        .disableBackoffStrategy()
        .customizeHttpClient: httpClientBuilder =>
          httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom()
            // .setResponseTimeout(Timeout.ofSeconds(15))
            .build())
        .build()
end C7NoAuthClient

object C7BasicAuthClient extends C7Client:

  def client =
    ZIO.attempt:
      val encodedCredentials = encodeCredentials("admin", "admin")
      val cl                 = ExternalTaskClient.create()
        .baseUrl("http://localhost:8080/engine-rest")
        .disableBackoffStrategy()
        .customizeHttpClient: httpClientBuilder =>
          httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom()
            .setResponseTimeout(Timeout.ofSeconds(15))
            .build())
            .setDefaultHeaders(List(new org.apache.hc.core5.http.message.BasicHeader(
              "Authorization",
              s"Basic $encodedCredentials"
            )).asJava)
        .build()
      cl

  private def encodeCredentials(username: String, password: String): String =
    val credentials = s"$username:$password"
    Base64.getEncoder.encodeToString(credentials.getBytes)
end C7BasicAuthClient

trait OAuth2Client extends C7Client, OAuthPasswordFlow:
  given WorkerLogger       = Slf4JLogger.logger(getClass.getName)
  def camundaRestUrl       = "http://localhost:8080/engine-rest"
  def maxTimeForAcquireJob = 500.millis
  def lockDuration: Long   = 30.seconds.toMillis
  def maxTasks: Int        = 10

  def addAccessToken = new HttpRequestInterceptor:
    override def process(request: HttpRequest, entity: EntityDetails, context: HttpContext): Unit =
      val token = adminToken().toOption.getOrElse("NO TOKEN")
      request.addHeader("Authorization", token)

  def client =
    ZIO
      .attempt:
        ExternalTaskClient.create()
          .baseUrl(camundaRestUrl)
          .maxTasks(maxTasks)
          //  .disableBackoffStrategy()
          .backoffStrategy(
            new ExponentialBackoffStrategy(
              100L,
              2.0,
              maxTimeForAcquireJob.toMillis
            )
          )
          .lockDuration(lockDuration)
          .customizeHttpClient: httpClientBuilder =>
            httpClientBuilder
              .addRequestInterceptorLast(addAccessToken)
              .build()
          .build()
      .tap: client =>
        ZIO.logInfo(s"Created C7 Client with maxTimeForAcquireJob: $maxTimeForAcquireJob")

end OAuth2Client
