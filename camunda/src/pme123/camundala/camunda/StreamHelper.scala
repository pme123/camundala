package pme123.camundala.camunda

import java.io.{ByteArrayInputStream, File, FileInputStream, InputStream}

import org.apache.commons.io.IOUtils
import pme123.camundala.model.bpmn.{CamundalaException, StaticFile}
import zio._

import scala.io.{BufferedSource, Source}
import scala.xml.{Elem, Node, XML}

case class StreamHelper(basePath: String) {

  def inputStream(staticFile: StaticFile): Option[InputStream] = {
    val file = new File(s"$basePath/${staticFile.pathWithName}")
    if (file.exists())
      Some(new FileInputStream(file)) // prefer from File so in development no restart is needed
    else {
      Option(getClass.getClassLoader.getResourceAsStream(staticFile.pathWithName))
    }
  }

  def inputStreamM(staticFile: StaticFile): IO[StreamHelperException, InputStream] =
    ZIO.fromOption(inputStream(staticFile))
      .orElseFail(StreamHelperException(s"Problem loading Static File: ${staticFile.pathWithName}"))

  def asString(staticFile: StaticFile): Option[String] =
    inputStream(staticFile).map(is =>
      (staticFile.includes.mkString("", "\n", "\n") +
        IOUtils.toString(is, "UTF-8"))
        .trim
    )

  def asStringM(staticFile: StaticFile): IO[StreamHelperException, String] =
    ZIO.fromOption(asString(staticFile))
      .orElseFail(StreamHelperException(s"Problem loading Static File: ${staticFile.pathWithName}"))


  def inputStream(xml: Node): InputStream =
    new ByteArrayInputStream(xml.toString.getBytes)

  def inputStreamManaged(staticFile: StaticFile): Option[Managed[Throwable, InputStream]] = {
    inputStream(staticFile).map(is =>
      Managed.make(Task.effect(is))(
        is => UIO.succeed(is.close())
      )
    )

  }

  def xml(staticFile: StaticFile): Task[Elem] = {
    xmlSource(staticFile).use(s =>
      ZIO.effect(XML.load(s.reader()))
        .mapError(ex => StreamHelperException(s"There is a Problem loading ${staticFile.pathWithName}", Some(ex)))
    )
  }

  private def xmlSource(staticFile: StaticFile): TaskManaged[BufferedSource] = {
    val file = new File(s"$basePath/${staticFile.pathWithName}")
    Managed.make(Task(
      if (file.exists())
        Source.fromFile(file) // prefer from File so in development no restart is needed
      else
        Source.fromResource(staticFile.pathWithName)
    ))(
      is => UIO.succeed(is.close())
    )
  }

  case class StreamHelperException(msg: String, override val cause: Option[Throwable] = None)
    extends CamundalaException

}
