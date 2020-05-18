package pme123.camundala.camunda

import java.io.{ByteArrayInputStream, InputStream}

import pme123.camundala.model.bpmn.{CamundalaException, StaticFile}
import zio.{Cause, Managed, Task, TaskManaged, UIO, ZIO}

import scala.io.{BufferedSource, Source}
import scala.xml.{Elem, Node, XML}

object StreamHelper {
  def inputStream(staticFile: StaticFile): InputStream =
    getClass.getClassLoader.getResourceAsStream(staticFile.pathWithName)

  def inputStream(xml: Node): InputStream =
    new ByteArrayInputStream(xml.toString.getBytes)

  def inputStreamManaged(staticFile: StaticFile): Managed[Throwable, InputStream] =
    Managed.make(Task.effect(getClass.getClassLoader.getResourceAsStream(staticFile.pathWithName)))(
      is => UIO.succeed(is.close())
    )

  def xml(staticFile: StaticFile): Task[Elem] =
    xmlSource(staticFile).use(s =>
        ZIO.effect(XML.load(s.reader()))
      .mapError(ex => StreamHelperException(s"There is a Problem loading ${staticFile.pathWithName}", Some(ex)))
    )

  private def xmlSource(staticFile: StaticFile): TaskManaged[BufferedSource] =
    Managed.make(Task.effect(Source.fromResource(staticFile.pathWithName)))(
      is => UIO.succeed(is.close())
    )

  case class StreamHelperException(msg: String, override val cause: Option[Throwable])
    extends CamundalaException

}
