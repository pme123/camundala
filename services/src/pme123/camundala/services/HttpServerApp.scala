package pme123.camundala.services

import zio._

object HttpServerApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    httpServer.serve()
      .provideLayer(ServicesLayers.httpServerLayer)
      .exitCode

}






