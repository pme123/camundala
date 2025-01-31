package camundala.worker.c8zio

import camundala.worker.c8zio.oauth.OAuthPasswordFlow
import camundala.worker.{Slf4JLogger, WorkerLogger}
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.*
import org.apache.hc.core5.http.*
import org.apache.hc.core5.http.protocol.HttpContext
import org.apache.hc.core5.util.Timeout
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.ExponentialErrorBackoffStrategy
import zio.ZIO

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

object OAuth2Client extends C7Client, OAuthPasswordFlow:
  given WorkerLogger = Slf4JLogger.logger(getClass.getName)
  lazy val fssoRealm: String = sys.env.getOrElse("FSSO_REALM", "0949")
  lazy val fssoBaseUrl = sys.env.getOrElse("FSSO_BASE_URL", s"http://host.lima.internal:8090")

  def addAccessToken = new HttpRequestInterceptor:
    override def process(request: HttpRequest, entity: EntityDetails, context: HttpContext): Unit =
      request.addHeader("Authorization", "Bearer " + adminToken().toOption.getOrElse("NO TOkEN"))

  def client =
    ZIO.attempt:
      ExternalTaskClient.create()
        .baseUrl("http://localhost:8080/engine-rest")
        .disableBackoffStrategy()
        .customizeHttpClient: httpClientBuilder =>
          httpClientBuilder
            .addRequestInterceptorLast(addAccessToken)
            .build()
        .build()
end OAuth2Client
