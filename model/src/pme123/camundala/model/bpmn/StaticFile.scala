package pme123.camundala.model.bpmn

import java.io.InputStream

import zio._

import scala.io.{BufferedSource, Source}
import scala.xml.{Elem, XML}

case class StaticFile(fileName: String, resourcePath: String) {
  /*
    def inputStream: TaskManaged[InputStream] =
      Managed.make(Task.effect(getClass.getClassLoader.getResourceAsStream(s"$resourcePath/$fileName")))(
        is => UIO.succeed(is.close())
      )
  */
  def inputStream: InputStream =
    getClass.getClassLoader.getResourceAsStream(s"$resourcePath/$fileName")

  def xml: Task[Elem] =
    xmlSource.use(s => Task.effect(XML.load(s.reader())))

  private def xmlSource: TaskManaged[BufferedSource] =
    Managed.make(Task.effect(Source.fromResource(s"$resourcePath/$fileName")))(
      is => UIO.succeed(is.close())
    )
}
