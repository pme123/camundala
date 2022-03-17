package camundala
package camunda

import bpmn.*
import camundala.camunda.CamundaMapperMacros.mapImpl
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}
import io.circe.generic.auto.*
import org.camunda.bpm.model.bpmn.builder.{
  AbstractFlowNodeBuilder,
  CallActivityBuilder
}
import org.camunda.bpm.model.bpmn.instance.camunda.{
  CamundaIn,
  CamundaInputOutput,
  CamundaInputParameter,
  CamundaOut,
  CamundaOutputParameter,
  CamundaScript
}
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

  extension (process: Process[?, ?])
    def bpmn =
      BpmnProcess(process)

    def bpmn(
        elements: (InOut[?, ?, ?] | BpmnInOut[?, ?])*
    ): BpmnProcess =
      BpmnProcess(process)
        .withElements(elements: _*)

  extension [In <: Product, Out <: Product](inOut: InOut[In, Out, ?])

    inline def mapIn[T, A](
        inline path: T => A,
        inline targetName: In => A
    ): BpmnInOut[In, Out] =
      ${
        mapImpl[In, Out, T, A, In](
          '{ BpmnInOut(inOut) },
          'path,
          'targetName,
          '{ false }
        )
      }

    inline def mapOut[T, A](
        inline path: Out => A,
        inline targetName: T => A
    ): BpmnInOut[In, Out] =
      ${
        mapImpl[In, Out, Out, A, T](
          '{ BpmnInOut(inOut) },
          'path,
          'targetName,
          '{ true }
        )
      }

  extension [In <: Product, Out <: Product](
      bpmnInOut: BpmnInOut[In, Out]
  )

    inline def mapIn[T, A](
        inline path: T => A,
        inline targetName: In => A
    ): BpmnInOut[In, Out] =
      ${ mapImpl('{ bpmnInOut }, 'path, 'targetName, '{ false }) }

    inline def mapOut[T, A](
        inline path: Out => A,
        inline targetName: T => A
    ): BpmnInOut[In, Out] =
      ${ mapImpl('{ bpmnInOut }, 'path, 'targetName, '{ true }) }

    inline def mapOut[T, A, B](
        inline path: Out => A,
        inline targetName: T => B
    ): BpmnInOut[In, Out] =
      ${ mapImpl('{ bpmnInOut }, 'path, 'targetName, '{ true }) }
  extension (bpmnProcess: BpmnProcess)

    def toCamunda: FromCamundable[Unit] =
      val cProc: CProcess = summon[CBpmnModelInstance]
        .getModelElementById(bpmnProcess.id)
      bpmnProcess.elements.collect {
        case ca @ BpmnInOut(_: CallActivity[?, ?], _, _) =>
          mergeCallActivity(ca)
      }

    def mergeCallActivity[In <: Product, Out <: Product](
        ca: BpmnInOut[In, Out]
    ): FromCamundable[Unit] =
      val cCA: CCallActivity = summon[CBpmnModelInstance]
        .getModelElementById(ca.id)
      println(s"CallActivity: $ca")
      merge(cCA)

      def merge(elem: CCallActivity): FromCamundable[Unit] =
        val builder
            : AbstractFlowNodeBuilder[CallActivityBuilder, CCallActivity] =
          elem.builder()

        def mergeInOut(
            isIn: Boolean,
            p: Product,
            mappers: Seq[PathMapper]
        ): FromCamundable[Unit] =
          p.productElementNames.foreach { v =>
            mapInOuts(isIn, builder, v, mappers)
          }

        println(s"TT ${ca.inOut.out}")
        val inout =
          summon[CBpmnModelInstance].newInstance(classOf[CamundaInputOutput])
        //builder.addExtensionElement(inout)
        //   mergeInputParams(inout, ca.inMappers)
        //   mergeOutputParams(inout, ca.outMappers)
        ca.inOut.in match
          case p: Product =>
            mergeInOut(true, p, ca.inMappers)
        ca.inOut.out match
          case p: Product =>
            println(s"TT $p")
            mergeInOut(false, p, ca.outMappers)

  def mapInOuts(
      isIn: Boolean,
      builder: AbstractFlowNodeBuilder[CallActivityBuilder, CCallActivity],
      paramName: String,
      mappers: Seq[PathMapper]
  ): FromCamundable[Unit] =
    println(s"MAPPING all: $mappers")

    val mappingInOut = mappers
      .filter(_.isInOutMapper)
      .filter(mp => {
        println(s"MP: $paramName $mp")
        if (isIn)
          mp.varName == paramName
        else
          mp.path.head match
            case PathEntry.PathElem(n) =>
              n == paramName
            case _ => false
      })
    println(s"MAPPING : $mappingInOut")
    if (mappingInOut.isEmpty)
      addInOut(isIn, builder, paramName)
    else
      mappingInOut.foreach(pm => addInOut(isIn, builder, paramName, Some(pm)))

  def addInOut(
      isIn: Boolean,
      builder: AbstractFlowNodeBuilder[CallActivityBuilder, CCallActivity],
      paramName: String,
      pathMapper: Option[PathMapper] = None
  ): FromCamundable[Unit] =
    if (isIn)
      addIn(builder, paramName, pathMapper)
    else
      addOut(builder, paramName, pathMapper)

  def addIn(
      builder: AbstractFlowNodeBuilder[CallActivityBuilder, CCallActivity],
      paramName: String,
      pathMapper: Option[PathMapper] = None
  ): FromCamundable[Unit] =
    val param: CamundaIn =
      summon[CBpmnModelInstance].newInstance(classOf[CamundaIn])
    pathMapper match
      case None => param.setCamundaSource(paramName)
      case Some(PathMapper(_, _, (PathEntry.PathElem(n)) :: Nil)) =>
        param.setCamundaSource(n)
      case Some(pm) => param.setCamundaSourceExpression(pm.printExpression())

    param.setCamundaTarget(paramName)
    builder.addExtensionElement(param)

  def addOut(
      builder: AbstractFlowNodeBuilder[CallActivityBuilder, CCallActivity],
      paramName: String,
      pathMapper: Option[PathMapper] = None
  ): FromCamundable[Unit] =
    val param: CamundaOut =
      summon[CBpmnModelInstance].newInstance(classOf[CamundaOut])
    pathMapper match // same as In but no common interface!?
      case Some(PathMapper(varName, _, (PathEntry.PathElem(n)) :: Nil)) =>
        param.setCamundaSource(paramName)
        param.setCamundaTarget(varName)
      case Some(pm @ PathMapper(varName, _, (PathEntry.PathElem(n)) :: _)) =>
        param.setCamundaSourceExpression(pm.printExpression())
        param.setCamundaTarget(varName)
      case _ =>
        param.setCamundaSource(paramName)
        param.setCamundaTarget(paramName)
    builder.addExtensionElement(param)
  /*
  mappers
        .foreach { case pm @ PathMapper(varName, _, _) =>
          val cp = summon[CBpmnModelInstance].newInstance(
            classOf[CamundaInputParameter]
          )
          cp.setCamundaName(varName)
         // inout.getCamundaInputParameters.add(cp)
          cp.setValue(inOutScript(pm))
        }*/

  def mergeInputParams(
      inout: CamundaInputOutput,
      mappers: Seq[PathMapper]
  ): FromCamundable[Unit] =
    mappers
      .foreach { case pm @ PathMapper(varName, _, _) =>
        val cp = summon[CBpmnModelInstance].newInstance(
          classOf[CamundaInputParameter]
        )
        cp.setCamundaName(varName)
        inout.getCamundaInputParameters.add(cp)
        cp.setValue(inOutScript(pm))
      }

  def mergeOutputParams(
      inout: CamundaInputOutput,
      mappers: Seq[PathMapper]
  ): FromCamundable[Unit] =
    mappers
      .foreach { case pm @ PathMapper(varName, _, _) =>
        val cp = summon[CBpmnModelInstance].newInstance(
          classOf[CamundaOutputParameter]
        )
        cp.setCamundaName(varName)
        inout.getCamundaOutputParameters.add(cp)
        cp.setValue(inOutScript(pm))
      }

  def inOutScript(
      pm: PathMapper
  ): FromCamundable[CamundaScript] =
    val script: CamundaScript =
      summon[CBpmnModelInstance].newInstance(classOf[CamundaScript])
    script.setCamundaScriptFormat("Groovy")
    script.setTextContent(pm.printGroovy())
    script

end GenerateCamundaBpmn
