package camundala.worker.c8zio

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder
import zio.{ZIO, ZIOAppDefault}

import java.net.URI
import java.time.Duration

object C8WorkerClient extends ZIOAppDefault:

  override def run: ZIO[Any, Any, Any] =
    ZIO.acquireReleaseWith(zeebeClient)(_.closeClient()): client =>
      for
        server <- ZIO.attempt(
                    client
                      .newTopologyRequest
                      .send
                      .join
                  ).forever.fork
        worker <- ZIO.attempt(client
                    .newWorker()
                    .jobType("publish-tweet")
                    .handler(ExampleJobHandler())
                    .timeout(Duration.ofSeconds(10))
                    .open()).fork
        _      <- worker.join
        _      <- server.join
      yield ()

  private lazy val zeebeClient =
    ZIO.attempt:
      ZeebeClient.newClientBuilder()
        .grpcAddress(URI.create(zeebeGrpc))
        .restAddress(URI.create(zeebeRest))
        .credentialsProvider(credentialsProvider)
        .build

  private lazy val zeebeGrpc    =
    "https://dbd4cad1-5621-4d66-b14e-71c92456939a.bru-2.zeebe.camunda.io:443"
  private lazy val zeebeRest    =
    "https://bru-2.zeebe.camunda.io:443/dbd4cad1-5621-4d66-b14e-71c92456939a/v2"
  private lazy val audience     = "zeebe.camunda.io"
  private lazy val clientId     = sys.env("CAMUNDA8_CLOUD_CLIENTID")
  private lazy val clientSecret = sys.env("CAMUNDA8_CLOUD_CLIENTSECRET")
  private lazy val oAuthAPI     = "https://login.cloud.camunda.io/oauth/token"

  private lazy val credentialsProvider =
    new OAuthCredentialsProviderBuilder()
      .authorizationServerUrl(oAuthAPI)
      .audience(audience)
      .clientId(clientId)
      .clientSecret(clientSecret)
      .build

  extension (client: ZeebeClient)
    def closeClient() =
      ZIO.succeed(if client != null then client.close() else ())

end C8WorkerClient
