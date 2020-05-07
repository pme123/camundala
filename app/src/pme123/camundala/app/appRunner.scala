package pme123.camundala.app

import zio._

object appRunner {
  type AppRunner = Has[Service]

  trait Service {
    def run(): Task[Unit]
  }

  def run(): RIO[AppRunner, Unit] =
    ZIO.accessM(_.get.run())
}
