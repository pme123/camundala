package camundala
package camunda

import bpmn.*
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}
import io.circe.generic.auto.*
import org.camunda.bpm.model.bpmn.instance.camunda.{CamundaIn, CamundaOut}
import sttp.tapir.generic.auto.*

import java.io.File
import scala.language.implicitConversions

trait GenerateCamundaBpmn extends BpmnDsl, ProjectPaths, App:

  def run(generateBpmns: Bpmn*): Unit =
    for bpmn <- generateBpmns
    yield
      println(s"// WITH IDS: ${bpmn.path}")
      val modelInstance = toCamunda(
        bpmn
      )
      println(s"// Generated: ${(generatedPath / bpmn.path.toIO.getName).toIO}")
      CBpmn.writeModelToFile(
        (generatedPath / bpmn.path.toIO.getName).toIO,
        modelInstance
      )

  private def toCamunda(
      bpmn: Bpmn
  ): BpmnModelInstance =
    implicit val modelInstance: BpmnModelInstance =
      CBpmn.readModelFromFile(bpmn.path.toIO)

    bpmn.processes.foreach(_.toCamunda)
    modelInstance
    
  extension (process: Process[?,?])
    def toCamunda: FromCamundable[Unit] =
      val cProc: CProcess = summon[CBpmnModelInstance]
        .getModelElementById(process.id)
      println(s"cProc: $cProc")
      process.inOuts.collect {
        case ca: CallActivity[?,?] => ca.toCamunda
      }

  extension (ca: CallActivity[?,?])
    def toCamunda: FromCamundable[Unit] =
      val cCA: CCallActivity = summon[CBpmnModelInstance]
        .getModelElementById(ca.id)
      println(s"CallActivity: $ca")
      println(s"cCA: $cCA")
      merge(cCA)

    private def merge(elem: CCallActivity): FromCamundable[Unit] =
      val builder = elem.builder()
      def mergeIn(p: Product): FromCamundable[Unit] =
        p.productElementNames.foreach { v =>
          val param: CamundaIn =
            summon[CBpmnModelInstance].newInstance(classOf[CamundaIn])
          param.setCamundaSource(v)
          param.setCamundaTarget(v)
          builder.addExtensionElement(param)
        }

      def mergeOut(p: Product): FromCamundable[Unit] =
        p.productElementNames.foreach { v =>
          val param: CamundaOut =
            summon[CBpmnModelInstance].newInstance(classOf[CamundaOut])
          param.setCamundaSource(v)
          param.setCamundaTarget(v)
          builder.addExtensionElement(param)
        }

      ca.in match
        case p: Product => mergeIn(p)
      ca.out match
        case p: Product => mergeOut(p)

  private def printInOut(ident: String): Unit =
    println(s"""
               |  val ${ident}Ident ="${ident}Ident"
               |  lazy val $ident = process(
               |    ${ident}Ident,
               |    in = NoInput(),
               |    out = NoOutput(),
               |    descr = None
               |  )
               |""".stripMargin)

end GenerateCamundaBpmn

