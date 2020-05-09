package pme123.camundala.examples.twitter

import java.io.File
import java.net.URLClassLoader
import java.nio.file.{Path, Paths}

import pme123.camundala.BuildInfo

import scala.concurrent.duration.Duration
import scala.reflect.internal.util.{AbstractFileClassLoader, BatchSourceFile}
import scala.tools.nsc.{Global, Settings}
import scala.tools.nsc.io.VirtualDirectory
import scala.tools.nsc.reporters.StoreReporter
import scala.concurrent.duration._

object EvaluatorApp extends App {

  println(s"          BuildInfo: ${BuildInfo.modelClasspath}")
  println(s"          BuildInfo options: ${BuildInfo.scalacOptions}")

  val artifacts =
    BuildInfo.modelClasspath.split("::").toSeq.distinct
      .map(str => Paths.get(str))
  val scalacOptions =
    BuildInfo.scalacOptions.split("::").toSeq.distinct

  (
    """object Hello{
      |println("hello world")
      |}""".stripMargin)

  lazy val eval  = new Evaluator(artifacts, scalacOptions, false, 20.seconds)

}

import scala.tools.nsc.Global
import scala.tools.nsc.reporters.StoreReporter
import scala.tools.nsc.io.{VirtualDirectory, AbstractFile}
import scala.reflect.internal.util.{NoPosition, BatchSourceFile, AbstractFileClassLoader}

import java.io.File
import java.nio.file.Path
import java.net.URLClassLoader
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.duration._

class Evaluator(artifacts: Seq[Path], scalacOptions: Seq[String], security: Boolean, timeout: Duration) {
import model._
  def apply(request: EvalRequest): EvalResponse = synchronized {
    if (request.code.isEmpty) EvalResponse.empty
    else {
      try {
        runTimeout(request.code)
      } catch {
        case NonFatal(e) ⇒ handleException(e)
      }
    }
  }

  private val secured = new Secured(security)

  private def eval(code: String): EvalResponse = {
    secured {
      compile(code)
    }
    val infos = check()
    if (!infos.contains(Error)) {
      // Look for static class implementing Instrumented
      def findEval: Option[(Instrumentation, String)] = {
        def removeExt(of: String) = {
          val classExt = ".class"
          if (of.endsWith(classExt)) of.slice(0, of.lastIndexOf(classExt))
          else of
        }

        def removeMem(of: String) = {
          of.slice("(memory)/".length, of.length)
        }

        def recurseFolders(file: AbstractFile): Set[AbstractFile] = {
          file.iterator.toSet.flatMap { fs ⇒
            if (fs.isDirectory)
              fs.toSet ++
                fs.filter(_.isDirectory).flatMap(recurseFolders).toSet
            else Set(fs)
          }
        }

        val instrClass =
          recurseFolders(target).
            map(_.path).
            map(((removeExt _) compose (removeMem _))).
            map(_.replace('/', '.')).
            filterNot(_.endsWith("$class")).
            find { n ⇒
              Try(classLoader.loadClass(n)).map(
                _.getInterfaces.exists(_ == classOf[Instrumented])
              ).getOrElse(false)
            }

        instrClass.map { c ⇒
          val cl = Class.forName(c, false, classLoader)
          val cons = cl.getConstructor()
          secured {
            val baos = new java.io.ByteArrayOutputStream()
            val ps = new java.io.PrintStream(baos)
            val result = Console.withOut(ps)(cons.newInstance().asInstanceOf[Instrumented].instrumentation$)
            (result, baos.toString("UTF-8"))
          }
        }
      }

      val (instrumentation, console) = findEval.getOrElse((Nil, ""))
      EvalResponse.empty.copy(
        instrumentation = instrumentation,
        console = console,
        complilationInfos = infos
      )
    } else {
      EvalResponse.empty.copy(complilationInfos = infos)
    }
  }

  private def runTimeout(code: String) =
    withTimeout {
      eval(code)
    }(timeout).getOrElse(EvalResponse.empty.copy(timeout = true))

  private def withTimeout[T](f: ⇒ T)(timeout: Duration): Option[T] = {
    val task = new FutureTask(new Callable[T]() {
      def call = f
    })
    val thread = new Thread(task)
    try {
      thread.start()
      Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
    } catch {
      case e: TimeoutException ⇒ None
    } finally {
      if (thread.isAlive) thread.stop()
    }
  }

  private def handleException(e: Throwable): EvalResponse = {
    def search(e: Throwable) = {
      e.getStackTrace.find(_.getFileName == "(inline)").map(v ⇒
        (e, Some(v.getLineNumber))
      )
    }

    def loop(e: Throwable): Option[(Throwable, Option[Int])] = {
      val s = search(e)
      if (s.isEmpty)
        if (e.getCause != null) loop(e.getCause)
        else Some((e, None))
      else s
    }

    EvalResponse.empty.copy(runtimeError = loop(e).map { case (err, line) ⇒
      RuntimeError(err.toString, line)
    })
  }

  private def check(): Map[Severity, List[CompilationInfo]] = {
    val infos =
      reporter.infos.map { info ⇒
        val pos = info.pos match {
          case NoPosition ⇒ None
          case _ ⇒ Some(RangePosition(info.pos.start, info.pos.point, info.pos.end))
        }
        (
          info.severity,
          info.msg,
          pos
        )
      }.toList
        .filterNot { case (sev, msg, _) ⇒
          // annoying
          sev == reporter.WARNING &&
            msg == ("a pure expression does nothing in statement " +
              "position; you may be omitting necessary parentheses")
        }.groupBy(_._1)
        .mapValues {
          _.map { case (_, msg, pos) ⇒ (msg, pos) }
        }

    def convert(infos: Map[reporter.Severity, List[(String, Option[RangePosition])]]): Map[Severity, List[CompilationInfo]] = {
      infos.map { case (k, vs) ⇒
        val sev = k match {
          case reporter.ERROR ⇒ Error
          case reporter.WARNING ⇒ Warning
          case reporter.INFO ⇒ Info
        }
        val info = vs map { case (msg, pos) ⇒
          CompilationInfo(msg, pos)
        }
        (sev, info)
      }
    }

    convert(infos)
  }

  private def reset(): Unit = {
    target.clear()
    reporter.reset()
    classLoader = new AbstractFileClassLoader(target, artifactLoader)
  }

  private def compile(code: String): Unit = {
    reset()
    val run = new compiler.Run
    val sourceFiles = List(new BatchSourceFile("(inline)", code))
    run.compileSources(sourceFiles)
  }

  private val reporter = new StoreReporter()
  private val settings = toSettings(artifacts, scalacOptions)
  private val artifactLoader = {
    val loaderFiles =
      settings.classpath.value.split(File.pathSeparator).map(a ⇒ {

        val node = new java.io.File(a)
        val endSlashed =
          if (node.isDirectory) node.toString + File.separator
          else node.toString

        new File(endSlashed).toURI().toURL
      })
    new URLClassLoader(loaderFiles, this.getClass.getClassLoader)
  }
  private val target = new VirtualDirectory("(memory)", None)
  private var classLoader: AbstractFileClassLoader = _
  settings.outputDirs.setSingleOutput(target)
  private val compiler = new Global(settings, reporter)
}

object model {
  type Instrumentation = List[(RangePosition, Render)]

  case class RangePosition(
                            start: Int,
                            point: Int,
                            end: Int
                          )

  sealed trait Severity
  final case object Info extends Severity
  final case object Warning extends Severity
  final case object Error extends Severity

  case class CompilationInfo(
                              message: String,
                              pos: Option[RangePosition]
                            )

  // TODO: stacktrace
  // stack: List[StackElement]
  // String  getClassName()
  // String  getFileName()
  // int getLineNumber()
  // String  getMethodName()
  // TODO: range pos ?
  case class RuntimeError(
                           message: String,
                           position: Option[Int]
                         )

  // TODO: scalacOptions & dependencies
  case class EvalRequest(
                          code: String
                        )

  sealed trait Render
  case class Value(v: String, className: String) extends Render
  case class Markdown(a: String, folded: Boolean = false) extends Render {
    def stripMargin = Markdown(a.stripMargin)
    def fold = copy(folded = true)
  }
  case class Html(a: String, folded: Boolean = false) extends Render {
    def stripMargin = copy(a = a.stripMargin)
    def fold = copy(folded = true)
  }
  case class Html2(a: String, folded: Boolean = false) extends Render {
    def stripMargin = copy(a = a.stripMargin)
    def fold = copy(folded = true)
  }

  case class EvalResponse(
                           complilationInfos: Map[Severity, List[CompilationInfo]],
                           timeout: Boolean,
                           runtimeError: Option[RuntimeError],
                           instrumentation: List[(RangePosition, Render)],
                           console: String
                         )
  object EvalResponse {
    val empty = EvalResponse(Map.empty, false, None, Nil, "")
  }

  // TODO: scalacOptions & dependencies
  case class TypeAtRequest(
                            code: String,
                            position: RangePosition
                          )

  case class TypeAtResponse(
                             val tpe: String
                           )

  // TODO: scalacOptions & dependencies
  case class CompletionRequest(
                                code: String,
                                position: RangePosition
                              )

  case class CompletionResponse(
                                 val name: String,
                                 signature: String
                               )

  case class KeepAlive(msg: String = "") extends AnyVal

  sealed trait RoomListEvent
  case class NewRoom(roomName: String, user: String) extends RoomListEvent
  case class CloseRoom(roomName: String) extends RoomListEvent
  case class UpdateRoom(roomName: String, users: Vector[String]) extends RoomListEvent
  case class SetRooms(rooms: Map[String, Vector[String]]) extends RoomListEvent

  sealed trait CollaborationEvent
  case class JoinedDoc(user: String) extends CollaborationEvent
  case class LeftDoc(user: String) extends CollaborationEvent
  case class SetDoc(doc: woot.WString) extends CollaborationEvent

  sealed trait DocChange extends CollaborationEvent
  // CursorChange user from to
  // HightLight user from to
  case class ChangeDoc(operation: woot.Operation) extends DocChange
  case class ChangeBatchDoc(operations: List[woot.Operation]) extends DocChange
  case object HeartBeat extends DocChange

  trait Instrumented {
    def instrumentation$: Instrumentation
    def offset$: Int
  }

  class FailedInstrumented(e: String) extends Instrumented {
    def instrumentation$ = List(
      RangePosition(0, 0, 0) -> Value(e, "String")
    )
    def offset$ = 0
  }
}