package pme123.camundala.camunda

import pme123.camundala.camunda.xml.XBpmn
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn._
import zio._
import zio.logging.{Logger, Logging}

import scala.xml.Elem

object bpmnGenerator {
  type BpmnGenerator = Has[Service]

  trait Service {
    def generate(xmlFile: StaticFile): Task[Bpmn]
  }

  type BpmnGeneratorDeps = Logging with AppConfig

  lazy val live: URLayer[BpmnGeneratorDeps, BpmnGenerator] =
    ZLayer.fromServices[Logger[String], appConfig.Service, Service] { (log, configService) =>

      (xmlFile: StaticFile) => for {
        config <- configService.get()
        xml: Elem <- StreamHelper(config.basePath).xml(xmlFile)
        bpmnId <- bpmnIdFromFilePath(xmlFile.fileName)
        processes <- XBpmn(xml).createProcesses()
        bpmn = Bpmn(bpmnId, xmlFile, processes)
        _ <- log.debug(
          s"""
             |Generated BPMN of $bpmnId
             |${"*" * 50}
             |${bpmn.generateDsl()}
             |${"*" * 50}
             |""".stripMargin)
      } yield bpmn

    }
}
