package camundala
package camunda

import bpmn.{Bpmn, BpmnDsl, Process}
import org.camunda.bpm.model.bpmn.{BpmnModelInstance, Bpmn as CBpmn}
import io.circe.generic.auto.*
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
        (generatedPath / os.up / ("_experimental_" + bpmn.path.toIO.getName)).toIO,
        modelInstance
      )

  private def toCamunda(
      bpmn: Bpmn
  ): BpmnModelInstance =
    implicit val modelInstance: BpmnModelInstance =
      CBpmn.readModelFromFile(bpmn.path.toIO)

    bpmn.processes.foreach(_.toCamunda())
    modelInstance
  /*

  private def fromCamunda(
      bpmnFile: File,
      outputPath: BpmnPath
  ): IO[FromCamundaException, Bpmn] = {
    (for {
      modelInstance <- ZIO(
        CBpmn.readModelFromFile(bpmnFile)
      )
      cProcesses <- ZIO(
        modelInstance
          .getModelElementsByType(classOf[CProcess])
          .asScala
          .toSeq
      )
      processes <- ZIO.collect(cProcesses) { p =>
        p.fromCamunda()(using modelInstance)
          .mapError(Some(_))
      }
      _ <- ZIO(
        CBpmn.writeModelToFile(new File(outputPath), modelInstance)
      )
    } yield bpmn(bpmnFile).processes(processes: _*))
      .mapError {
        case Some(ex: FromCamundaException) => ex
        case t: Throwable =>
          t.printStackTrace
          FromCamundaException(t.getMessage)
      }
  }
   */
  extension (process: Process[?,?])
    def toCamunda(): FromCamundable[Unit] =
      val cProc: CProcess = summon[CBpmnModelInstance]
        .getModelElementById(process.id)
      println(s"cProc: $cProc")

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

  /*     for {
          ident <- camundaProcess.createIdent()
          startEvents <- createElements(classOf[CStartEvent], startEvent)
          userTasks <- createElements(classOf[CUserTask], userTask)
          serviceTasks <- createElements(
            classOf[CServiceTask],
            serviceTask
          )
          scriptTasks <- createElements(classOf[CScriptTask], scriptTask)
          callActivities <- createElements(classOf[CCallActivity], callActivity)
          businessRuleTasks <- createElements(
            classOf[CBusinessRuleTask],
            businessRuleTask
          )
          exclusiveGateways <- createElements(
            classOf[CExclusiveGateway],
            exclusiveGateway
          )
          parallelGateways <- createElements(
            classOf[CParallelGateway],
            parallelGateway
          )
          endEvents <- createElements(classOf[CEndEvent], endEvent)
          sequenceFlows <- createElements(
            classOf[CSequenceFlow],
            sequenceFlow
          )
        } yield process(ident)
          .nodes(
            startEvents ++
              userTasks ++
              serviceTasks ++
              scriptTasks ++
              callActivities ++
              businessRuleTasks ++
              exclusiveGateways ++
              parallelGateways ++
              endEvents: _*
          )
          .flows(sequenceFlows: _*)
   */
  private def createElements[T <: CFlowElement, C](
      clazz: Class[T],
      constructor: String
  ): FromCamundable[Unit] = {
    val elems = summon[CBpmnModelInstance]
      .getModelElementsByType(clazz)
      .asScala
      .toSeq
    elems.foreach { fe =>
      fe.createIdent()
    }
  }

  extension (process: CProcess)
    def createIdent(): String =
      val ident = identString(Option(process.getName), process)
      process.setId(ident)
      ident

  extension (element: CFlowElement)
    def generateIdent(): String =
      identString(Option(element.getName), element)

    def createIdent(): Unit =
      val ident: String =
        element match
          case flow: CSequenceFlow =>
            flow.createIdent()
          case _ =>
            generateIdent()
      element.setId(ident)
      printInOut(ident)

  extension (element: CSequenceFlow)
    def createIdent() =
      val ident = element.generateIdent()
      val sourceIdent = element.getSource.generateIdent()
      val targetIdent = element.getTarget.generateIdent()
      val newIdent = s"${ident}__${sourceIdent}__${targetIdent}"
      element.setId(newIdent)
      newIdent

  def identString(name: Option[String], camObj: CBaseElement): String =
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

end GenerateCamundaBpmn
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
