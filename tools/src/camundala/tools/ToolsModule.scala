package camundala.tools

import camundala.dsl.Bpmn
import zio._
import zio.logging.{Logger, Logging}

object ToolsModule {
  type Tools = Has[Service]

  trait Service {
    def generateDsl(bpmns: Bpmn*): UIO[String]
  }

  def generateDsl(bpmns: Bpmn*): URIO[Tools, String] =
    ZIO.accessM(_.get.generateDsl(bpmns:_*))

  type ToolsDeps = Logging

  lazy val live: RLayer[ToolsDeps, Tools] = ZLayer.fromService[Logger[String], Service] {
    log =>

      new Service {
        def generateDsl(bpmns: Bpmn*): UIO[String] =
          UIO(GeneratesDsl.generate(bpmns: _*))
            .tap(bpmnStr => log.info(bpmnStr))
      }
  }
}
