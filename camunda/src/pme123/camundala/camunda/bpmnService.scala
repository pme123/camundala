package pme123.camundala.camunda

import pme123.camundala.camunda.bpmn.ValidateWarnings
import pme123.camundala.camunda.xml.{XBpmn, XMergeResult}
import pme123.camundala.model.processRegister.ProcessRegister
import pme123.camundala.model.{Bpmn, CamundalaException, processRegister}
import zio._

import scala.xml.Elem

object bpmnService {
  type BpmnService = Has[Service]

  trait Service {
    def mergeBpmn(bpmnXml: Elem): Task[XMergeResult]
  }

  def mergeBpmn(bpmnXml: Elem): RIO[BpmnService, XMergeResult] =
    ZIO.accessM(_.get.mergeBpmn(bpmnXml))

  lazy val live: RLayer[ProcessRegister, BpmnService] =
    ZLayer.fromService[processRegister.Service, Service] {
      register =>

        new Service {
          def mergeBpmn(bpmnXml: Elem): Task[XMergeResult] = {
            for {
              xBpmn <- ZIO.effect(XBpmn(bpmnXml))
              processes <-
                ZIO.collectAll(xBpmn.processes.map(p =>
                  for {
                    p <- register.requestProcess(p.id)
                  } yield p
                )).map(_.filter(_.nonEmpty).map(_.get))
              mergeResult = xBpmn.merge(Bpmn(processes))
              _ = printf(s"mergeResult: ${mergeResult.xmlNode}")
            } yield mergeResult

          }
        }
    }

  case class BpmnServiceException(msg: String, validateErrors: ValidateWarnings)
    extends CamundalaException

}
