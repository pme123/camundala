package pme123.camundala.camunda

import java.io.{File, PrintWriter}

import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn.{filePathFromBpmnId, _}
import pme123.camundala.model.register.bpmnRegister
import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): Task[MergeResult]

    def mergeBpmn(bpmnId: BpmnId): Task[MergeResult]

    def generateBpmn(bpmnId: BpmnId): Task[List[String]]

    def validateBpmn(bpmnId: BpmnId): Task[ValidateWarnings]

  }

  def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId, bpmnXml))

  def mergeBpmn(bpmnId: BpmnId): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId))

  def generateBpmn(bpmnId: BpmnId): RIO[BpmnService, List[String]] =
    ZIO.accessM(_.get.generateBpmn(bpmnId))

  def validateBpmn(bpmnId: BpmnId): RIO[BpmnService, ValidateWarnings] =
    ZIO.accessM(_.get.validateBpmn(bpmnId))

  type BpmnServiceDeps = BpmnRegister with AppConfig

  lazy val live: RLayer[BpmnServiceDeps, BpmnService] =
    ZLayer.fromServices[bpmnRegister.Service, appConfig.Service, Service] {
      (register, configService) =>

        new Service {
          def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              xMergeResult <- merge(bpmnXml, maybeBpmn, bpmnId)
              f <- filePathFromBpmnId(bpmnId)
              fileName = maybeBpmn.map(_.xml.fileName).getOrElse(f)
            } yield MergeResult(fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def mergeBpmn(bpmnId: BpmnId): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              bpmn <- if (maybeBpmn.isDefined)
                UIO(maybeBpmn.get)
              else ZIO.fail(BpmnServiceException(s"There is no BPMN $bpmnId in the BPMN Register"))
              config <- configService.get()
              xml <- StreamHelper(config.basePath).xml(bpmn.xml)
              xMergeResult <- merge(xml, maybeBpmn, bpmnId)
            } yield MergeResult(bpmn.xml.fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def generateBpmn(bpmnId: BpmnId): Task[List[String]] =
            for {
              mergeResult <- mergeBpmn(bpmnId)
              config <- configService.get()
              staticFiles <- ZIO.foreach(mergeResult.maybeBpmn.toList.flatMap(_.staticFiles))(st =>
                ZIO((s"${config.basePath}/_generated/${st.pathWithName}", StreamHelper(config.basePath).asString(st))))
              bpmnFile <- ZIO.fromOption(mergeResult.maybeBpmn.map(bpmn => (s"${config.basePath}/_generated/${bpmn.xml.pathWithName}", mergeResult.xmlElem.toString)))
                .mapError(_ => BpmnServiceException(s"There is no BPMN with id $bpmnId"))
              paths <- ZIO.foreach(staticFiles :+ bpmnFile) { case (path, content) =>
                ZIO(new File(path).getParentFile.mkdirs()) *>
                  ZManaged.make(ZIO(new PrintWriter(new File(path)))
                  )(pw => UIO(pw.close()))
                    .use(pw =>
                      ZIO(pw.write(content))
                        .map(_ => path)
                    )
              }
            } yield paths

          def validateBpmn(bpmnId: BpmnId): Task[ValidateWarnings] =
            for {
              xMergeResult <- mergeBpmn(bpmnId)
            } yield xMergeResult.warnings

          private def merge(xml: Elem, maybeBpmn: Option[Bpmn], bpmnId: BpmnId) =
            for {
              xBpmn <- ZIO.effect(XBpmn(xml))
              xMergeResult: XMergeResult <- maybeBpmn.map(xBpmn.merge)
                .getOrElse(UIO(XMergeResult(xml, ValidateWarnings(s"There is no BPMN in the Registry with the BPMN id $bpmnId"))))
            } yield xMergeResult
        }
    }

  case class BpmnServiceException(msg: String)
    extends CamundalaException

}
