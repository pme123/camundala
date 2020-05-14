package pme123.camundala.camunda

import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.bpmn.{fileNameFromBpmnId, _}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): Task[MergeResult]

    def mergeBpmn(bpmnId: BpmnId): Task[MergeResult]

    def validateBpmn(bpmnId: BpmnId): Task[ValidateWarnings]

  }

  def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId, bpmnXml))

  def mergeBpmn(bpmnId: BpmnId): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId))

  def validateBpmn(bpmnId: BpmnId): RIO[BpmnService, ValidateWarnings] =
    ZIO.accessM(_.get.validateBpmn(bpmnId))


  lazy val live: RLayer[BpmnRegister, BpmnService] =
    ZLayer.fromService[bpmnRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(bpmnId: BpmnId, bpmnXml: Elem): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              xMergeResult <- merge(bpmnXml, maybeBpmn, bpmnId)
              f <- fileNameFromBpmnId(bpmnId)
              fileName = maybeBpmn.map(_.xml.fileName).getOrElse(f)
            } yield MergeResult(fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def mergeBpmn(bpmnId: BpmnId): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              bpmn <- if (maybeBpmn.isDefined)
                UIO(maybeBpmn.get)
              else ZIO.fail(BpmnServiceException(s"There is no BPMN $bpmnId in the BPMN Register"))
              xml <- StreamHelper.xml(bpmn.xml)
              xMergeResult <- merge(xml, maybeBpmn, bpmnId)
            } yield MergeResult(bpmn.xml.fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

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
