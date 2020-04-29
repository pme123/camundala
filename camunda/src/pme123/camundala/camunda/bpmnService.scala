package pme123.camundala.camunda

import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.model.{CamundalaException, bpmnRegister}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(fileName:String, bpmnXml: Elem): Task[MergeResult]
  }

  def mergeBpmn(fileName:String, bpmnXml: Elem): RIO[BpmnService, MergeResult] =
    ZIO.accessM(_.get.mergeBpmn(fileName, bpmnXml))

  lazy val live: RLayer[BpmnRegister, BpmnService] =
    ZLayer.fromService[bpmnRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(fileName:String, bpmnXml: Elem): Task[MergeResult] = {
            for {
              xBpmn <- ZIO.effect(XBpmn(bpmnXml))
              maybeBpmn <- register.requestBpmn(fileName)
              xMergeResult = maybeBpmn.map(xBpmn.merge)
                .getOrElse(XMergeResult(bpmnXml, ValidateWarnings(s"There is no BPMN in the Registry with the file name $fileName")))
            } yield MergeResult(xMergeResult.xmlNode, maybeBpmn, xMergeResult.warnings)

          }
        }
    }

  case class BpmnServiceException(msg: String, validateErrors: ValidateWarnings)
    extends CamundalaException

}
