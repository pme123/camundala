package camundala.worker

import camundala.BuildInfo
import zio.ZIO.*
import zio.{ZIOAppArgs, ZIOAppDefault, ZLayer}

trait WorkerApp extends ZIOAppDefault:
  def applicationName: String = getClass.getName.split('.').take(2).mkString("-")
  // a list of registries for each worker implementation
  def workerRegistries: Seq[WorkerRegistry]
  // list all the workers you want to register
  def workers(dWorkers: (WorkerDsl[?, ?] | Seq[WorkerDsl[?, ?]])*): Unit =
    theWorkers = dWorkers
      .flatMap:
        case d: WorkerDsl[?, ?] => Seq(d)
        case s: Seq[?]          => s.collect { case d: WorkerDsl[?, ?] => d }
      .toSet

  def dependencies(workerApps: WorkerApp*): Unit =
    theDependencies = workerApps

  protected var theWorkers: Set[WorkerDsl[?, ?]] = Set.empty
  protected var theDependencies: Seq[WorkerApp]  = Seq.empty

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = ZioLogger.logger

  override def run: ZIO[Any, Any, Any] =
    (for
      _ <- logInfo(banner)
      _ <- foreachParDiscard(workerRegistries): registry =>
             registry.register((theDependencies :+ this).flatMap(_.theWorkers).toSet)
    yield ()).provideLayer(fixedThreadExecutorLayer)

  private lazy val banner =
    s"""
       |
       |..#######..########...######..##.....##.########..######...######.....###....##..........###...
       |.##.....##.##.....##.##....##.##.....##.##.......##....##.##....##...##.##...##.........##.##..
       |.##.....##.##.....##.##.......##.....##.##.......##.......##........##...##..##........##...##.
       |.##.....##.########..##.......#########.######....######..##.......##.....##.##.......##.....##
       |.##.....##.##...##...##.......##.....##.##.............##.##.......#########.##.......#########
       |.##.....##.##....##..##....##.##.....##.##.......##....##.##....##.##.....##.##.......##.....##
       |..#######..##.....##..######..##.....##.########..######...######..##.....##.########.##.....##
       |
       |                                                        >>> DOMAIN DRIVEN PROCESS ORCHESTRATION
       |  $applicationName
       |
       |  Camundala: ${BuildInfo.version}
       |  Scala: ${BuildInfo.scalaVersion}
       |""".stripMargin
end WorkerApp
