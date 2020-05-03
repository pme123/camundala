package pme123.camundala.camunda

import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.bpmn.{Bpmn, CamundalaException, bpmnRegister}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(fileName: String, bpmnXml: Elem): Task[MergeResult]

    def mergeBpmn(fileName: String): Task[MergeResult]
  }

  def mergeBpmn(fileName: String, bpmnXml: Elem): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(fileName, bpmnXml))

  def mergeBpmn(fileName: String): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(fileName))

  lazy val live: RLayer[BpmnRegister, BpmnService] =
    ZLayer.fromService[bpmnRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(fileName: String, bpmnXml: Elem): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(fileName)
              xMergeResult <- merge(bpmnXml, maybeBpmn, fileName)
            } yield MergeResult(fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          def mergeBpmn(fileName: String): Task[MergeResult] = {
            for {
              maybeBpmn <- register.requestBpmn(fileName)
              if maybeBpmn.isDefined
              bpmn = maybeBpmn.get
              xml <- bpmn.xml.xml
              xMergeResult <- merge(xml, maybeBpmn, fileName)
            } yield MergeResult(fileName, xMergeResult.xmlElem, maybeBpmn, xMergeResult.warnings)
          }

          private def merge(xml: Elem, maybeBpmn: Option[Bpmn], fileName:String) =
            for {
              xBpmn <- ZIO.effect(XBpmn(xml))
              xMergeResult: XMergeResult <- maybeBpmn.map(xBpmn.merge)
                .getOrElse(UIO(XMergeResult(xml, ValidateWarnings(s"There is no BPMN in the Registry with the file name $fileName"))))
            } yield xMergeResult
        }
    }

  case class BpmnServiceException(msg: String, validateErrors: ValidateWarnings)
    extends CamundalaException

}
