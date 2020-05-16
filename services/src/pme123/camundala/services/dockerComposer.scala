package pme123.camundala.services

import pme123.camundala.model.deploy.{DockerConfig, Url}
import pme123.camundala.app.sttpBackend.SttpTaskBackend
import sttp.client._
import zio.clock.Clock
import zio.duration._
import zio.logging.Logging
import zio.{logging, _}

import scala.sys.process
import scala.sys.process.Process

object dockerComposer {
  type DockerComposer = Has[Service]

  trait Service {
    def runDockerUp(dockerConfig: DockerConfig): RIO[Clock, String]

    def runDockerStop(dockerConfig: DockerConfig): Task[String]

    def runDockerDown(dockerConfig: DockerConfig): Task[String]
  }

  def runDockerUp(dockerConfig: DockerConfig): RIO[DockerComposer with Clock, String] =
    ZIO.accessM(_.get.runDockerUp(dockerConfig))

  def runDockerStop(dockerConfig: DockerConfig): RIO[DockerComposer, String] =
    ZIO.accessM(_.get.runDockerStop(dockerConfig))

  def runDockerDown(dockerConfig: DockerConfig): RIO[DockerComposer, String] =
    ZIO.accessM(_.get.runDockerDown(dockerConfig))

  type DockerComposerDeps = Has[SttpTaskBackend] with Logging

  lazy val live: URLayer[DockerComposerDeps, DockerComposer] =
    ZLayer.fromServices[logging.Logger[String], SttpTaskBackend, Service]{ (log, backend) =>
        new Service {
          implicit def sttpBackend: SttpBackend[Task, Nothing, NothingT] = backend

          def runDockerUp(dockerConfig: DockerConfig): RIO[Clock, String] =
            for {
              result <- dockerCompose(dockerConfig, "up -d").map(_.!!)
              _ <- dockerConfig.maybeReadyUrl.map(url =>
                  for {
                    _ <- log.info(s"Wait that ${url} is ready.")
                    _ <- pageIsReady(url)
                  } yield ()
              ).getOrElse(ZIO.unit)
            } yield result

          def runDockerStop(dockerConfig: DockerConfig): Task[String] =
            dockerCompose(dockerConfig, "down").map(_.!!)

          def runDockerDown(dockerConfig: DockerConfig): Task[String] =
            dockerCompose(dockerConfig, "down", "-v").map(_.!!)

          private def dockerCompose(config: DockerConfig, command: String, args: String*): Task[process.ProcessBuilder] =
            ZIO.effect(
              Process("docker-compose " +
                config.composeFilesString() +
                s" --project-directory ${config.dockerDir}" +
                s" -p ${config.projectName} " +
                command +
                " --remove-orphans" +
                args.mkString(" ", " ", ""))
            )

          private def pageIsReady(url: Url, attempt: Int = 50): RIO[Clock, Unit] = {
            val uri = uri"$url"
            (for {
              _ <- log.debug(s"Check $url")
              _ <- basicRequest.get(uri).send()
              _ <- log.info(s"$url is ready to use!")
            } yield ())
              .catchAll(t =>
              if (attempt == 0)
                ZIO.fail(DockerComposerException(s"\n$url could not be reached!", Some(t)))
              else
                log.info(s"still waiting ...") *>
                  ZIO.sleep(3.second) *>
                  pageIsReady(url, attempt - 1)
            )
          }
        }
    }

  case class DockerComposerException(msg: String, maybeCause: Option[Throwable] = None) extends RuntimeException(msg, maybeCause.orNull)

}