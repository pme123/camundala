package pme123.camundala.camunda

import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.bpmn.{Bpmn, CamundalaException, bpmnRegister}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(bpmnId: String, bpmnXml: Elem): Task[MergeResult]

    def mergeBpmn(bpmnId: String): Task[MergeResult]

    def validateBpmn(bpmnId: String): Task[ValidateWarnings]

  }

  def mergeBpmn(bpmnId: String, bpmnXml: Elem): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId, bpmnXml))

  def mergeBpmn(bpmnId: String): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnId))

  def validateBpmn(bpmnId: String): RIO[BpmnService, ValidateWarnings] =
    ZIO.accessM(_.get.validateBpmn(bpmnId))


  lazy val live: RLayer[BpmnRegister, BpmnService] =
    ZLayer.fromService[bpmnRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(bpmnId: String, bpmnXml: Elem): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              xMergeResult <- merge(bpmnXml, maybeBpmn, bpmnId)
            } yield MergeResult(bpmnId, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def mergeBpmn(bpmnId: String): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(bpmnId)
              bpmn <- if (maybeBpmn.isDefined)
                UIO(maybeBpmn.get)
              else ZIO.fail(BpmnServiceException(s"There is no BPMN $bpmnId in the BPMN Register"))
              xml <- bpmn.xml.xml
              xMergeResult <- merge(xml, maybeBpmn, bpmnId)
            } yield MergeResult(bpmnId, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def validateBpmn(bpmnId: String): Task[ValidateWarnings] =
            for {
              xMergeResult <- mergeBpmn(bpmnId)
            } yield xMergeResult.warnings

          private def merge(xml: Elem, maybeBpmn: Option[Bpmn], bpmnId: String) =
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
