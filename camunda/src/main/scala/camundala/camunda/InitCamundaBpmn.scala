package camundala
package camunda

import bpmn.*
import domain.*
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}
import os.RelPath
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import java.io.File
import scala.language.implicitConversions
import scala.util.matching.Regex
import scala.jdk.CollectionConverters.*

trait InitCamundaBpmn extends BpmnDsl, ProjectPaths, App:

  def avoidCreateIdRegex: Regex = "".r

  def run(name: String): Unit =
    val bpmns: Seq[(String, Seq[Process[?, ?]])] =
      for cawemoFile <- cawemoBpmns(cawemoPath.toIO)
      yield
        println(s"// CAWEMO: $cawemoFile")
        Path(cawemoFile)
        val processes = fromCamunda(
          cawemoFile
        )

        cawemoFile.getName -> processes
    printGenerator(name, bpmns)

  private def cawemoBpmns(cawemoFolder: File): Seq[File] =
    if (cawemoFolder.isDirectory)
      cawemoFolder
        .listFiles(new FilenameFilter {
          def accept(dir: File, name: String): Boolean =
            name.endsWith(".bpmn")
        })
        .toSeq
    else
      throw IllegalArgumentException(
        s"The cawemoFolder must be a directory! -> $cawemoFolder"
      )

  private def fromCamunda(
      cawemoFile: File
  ) =
    implicit val modelInstance: BpmnModelInstance =
      CBpmn.readModelFromFile(cawemoFile)
    val cProcesses = modelInstance
      .getModelElementsByType(classOf[CProcess])
      .asScala
      .filter(_.isExecutable())
      .toSeq
    val processes: Seq[Process[?, ?]] = cProcesses.map(_.fromCamunda())
    CBpmn.writeModelToFile(
      (withIdPath / cawemoFile.getName).toIO,
      modelInstance
    )
    processes

  extension (camundaProcess: CProcess)
    def fromCamunda(): FromCamundable[Process[?, ?]] =
      val ident = camundaProcess.createIdent()
      process(ident).copy(elements =
        createInOuts(classOf[CUserTask], UserTask.init) ++
          createInOuts(classOf[CServiceTask], ServiceTask.init) ++
          createInOuts(classOf[CCallActivity], CallActivity.init) ++
          createInOuts(classOf[CBusinessRuleTask], DecisionDmn.init) ++
          createElements(classOf[CEndEvent], EndEvent.init)
      )

  private def createInOuts[
      T <: CFlowElement,
      C,
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      clazz: Class[T],
      constructor: String => InOut[In, Out, ?]
  ): FromCamundable[Seq[InOut[In, Out, ?]]] = {
    val elems: Seq[T] = elements(clazz)
    elems.map { fe =>
      constructor(fe.createIdent())
    }
  }

  private def createElements[T <: CFlowElement](
      clazz: Class[T],
      constructor: String => ProcessNode
  ): FromCamundable[Seq[ProcessNode]] = {
    val elems: Seq[T] = elements(clazz)
    elems.map { fe =>
      constructor(fe.createIdent())
    }
  }
  private def elements[T <: CFlowElement](
      clazz: Class[T]
  ): FromCamundable[Seq[T]] =
    summon[CBpmnModelInstance]
      .getModelElementsByType(clazz)
      .asScala
      .toSeq

  extension (process: CProcess)
    def createIdent(): String =
      val ident = identString(Option(process.getName), process)
      process.setId(ident)
      ident

  extension (element: CFlowElement)
    def generateIdent(): String =
      identString(Option(element.getName), element)

    def createIdent(): String =
      val ident: String =
        element match
          case flow: CSequenceFlow =>
            flow.createIdent()
          case _ =>
            generateIdent()
      element.setId(ident)
      ident

  extension (element: CSequenceFlow)
    def createIdent() =
      val ident = element.generateIdent()
      val sourceIdent = element.getSource.generateIdent()
      val targetIdent = element.getTarget.generateIdent()
      val newIdent = s"${ident}__${sourceIdent}__${targetIdent}"
      element.setId(newIdent)
      newIdent

  def identString(name: Option[String], camObj: CBaseElement): String =
    if (avoidCreateIdRegex.matches(camObj.getId()))
      camObj.getId() // only adjust id if you want it
    else
      val elemKey: String =
        camObj.getElementType.getTypeName.capitalize.filter(c =>
          s"$c".matches("[A-Z]")
        )

      name match
        case Some(n) =>
          n.split("[^a-zA-Z0-9]")
            .map(_.capitalize)
            .mkString + elemKey
        case None =>
          camObj.getId

  private def printGenerator(
      name: String,
      bpmns: Seq[(String, Seq[Process[?, ?]])]
  ): Unit =
    println(s"""
import camundala.bpmn.*

import camundala.camunda.GenerateCamundaBpmn
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object ${name}GenerateCamundaBpmnApp extends GenerateCamundaBpmn:

  val projectPath = ${("pwd" +: projectPath
      .relativeTo(pwd)
      .segments
      .map(s => s"\"$s\""))
      .mkString(" / ")}
  import ${name}Domain.*
  run(${bpmns
      .map { case (fileName, procs) =>
        s"""Bpmn(withIdPath / "$fileName", ${procs
          .map(p => identName(p.id))
          .mkString("", ".bpmn, ", ".bpmn")})"""
      }
      .mkString(",\n         ")})

end ${name}GenerateCamundaBpmnApp
object ${name}Domain extends BpmnDsl:

${bpmns
      .map(bpmn =>
        s"  // ${bpmn._1}\n" +
          bpmn._2
            .map { p =>
              printInOut(p) +
                p.elements
                  .map {
                    case io: InOut[?, ?, ?] => printInOut(io)
                    case e: ProcessNode => printElem(e)
                  }
                  .mkString("\n", "\n", "")
            }
            .mkString("\n")
      )
      .mkString("\n")}

end ${name}Domain
""")
  //only needed if the id was avoided to create!
  private def identName(id: String) =
    id.split("[^a-zA-Z0-9]")
      .map(_.capitalize)
      .mkString

  private def printInOut(inOut: InOut[?, ?, ?]): String =
    s"""  val ${identName(inOut.id)}Ident ="${inOut.id}"
       |  lazy val ${identName(inOut.id)} = ${inOut.label}(
       |    ${identName(inOut.id)}Ident,
       |    in = NoInput(),
       |    out = NoOutput(),
       |    descr = None
       |  )
       |""".stripMargin

  private def printElem(processElement: ProcessNode): String =
    s"""  val ${identName(processElement.id)}Ident ="${processElement.id}"
       |  lazy val ${identName(processElement.id)} = ${processElement.label}(
       |    ${identName(processElement.id)}Ident,
       |    descr = None
       |  )
       |""".stripMargin

end InitCamundaBpmn

/*
case class FromCamundaRunner(fromCamundaConfig: FromCamundaConfig)
    extends FromCamundaBpmn:

  def run(): ZIO[zio.console.Console, FromCamundaException, Seq[Bpmn]] =
    (for {
      _: Any <- putStrLn(
        s"Start From Camunda BPMNs from ${fromCamundaConfig.cawemoFolder}"
      )
      bpmns: Seq[Bpmn] <- fromCamunda(fromCamundaConfig)
      _: Any <- putStrLn(
        s"Generated BPMNs to ${fromCamundaConfig.withIdFolder}"
      )
    } yield (bpmns))
      .mapError { case t: Throwable =>
        t.printStackTrace
        FromCamundaException(t.getMessage)
      }

end FromCamundaRunner

case class FromCamundaConfig(
    cawemoFolder: BpmnPath,
    withIdFolder: BpmnPath
)

case class FromCamundaException(msg: String)
 */
