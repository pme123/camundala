package pme123.camundala.camunda

import pme123.camundala.camunda.xml.{ValidateWarnings, XBpmn, XMergeResult}
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.model.{CamundalaException, bpmnRegister}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(fileName:String, bpmnXml: Elem): Task[XMergeResult]
  }

  def mergeBpmn(fileName:String, bpmnXml: Elem): RIO[BpmnService, XMergeResult] =
    ZIO.accessM(_.get.mergeBpmn(fileName, bpmnXml))

  lazy val live: RLayer[BpmnRegister, BpmnService] =
    ZLayer.fromService[bpmnRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(fileName:String, bpmnXml: Elem): Task[XMergeResult] = {
            for {
              xBpmn <- ZIO.effect(XBpmn(bpmnXml))
              maybeBpmn <- register.requestBpmn(fileName)
              mergeResult = maybeBpmn.map(xBpmn.merge)
                .getOrElse(XMergeResult(bpmnXml, ValidateWarnings(s"There is no BPMN in the Registry with the file name $fileName")))
            } yield mergeResult

          }
        }
    }

  case class BpmnServiceException(msg: String, validateErrors: ValidateWarnings)
    extends CamundalaException

}
