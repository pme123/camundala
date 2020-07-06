package pme123.camundala.cli

import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.services.ServicesLayers
import pme123.camundala.services.StandardApp.StandardAppDeps
import zio.console.Console
import zio.{ExitCode, ZIO, ZLayer}

import scala.annotation.nowarn

trait StandardCliApp extends zio.App {

  protected def appRunnerLayer: ZLayer[StandardAppDeps, Nothing, AppRunner]

  protected def title: String

  protected def ident: String

  protected def projectInfo: ProjectInfo

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    runCli
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(ServicesLayers.appDepsLayer >>> CliLayers.cliLayer(appRunnerLayer))
      .exitCode

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo)
}
