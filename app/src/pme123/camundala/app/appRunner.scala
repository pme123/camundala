package pme123.camundala.app

import zio._

object appRunner {
  type AppRunner = Has[Service]

  trait Service {
    def start(): Task[Unit]

    def stop(): Task[Unit]

    def restart(): Task[Unit]

    def update(): Task[Unit]
  }

  def start(): RIO[AppRunner, Unit] =
    ZIO.accessM(_.get.start())

  def stop(): RIO[AppRunner, Unit] =
    ZIO.accessM(_.get.stop())

  def restart(): RIO[AppRunner, Unit] =
    ZIO.accessM(_.get.restart())

  def update(): RIO[AppRunner, Unit] =
    ZIO.accessM(_.get.update())
}
