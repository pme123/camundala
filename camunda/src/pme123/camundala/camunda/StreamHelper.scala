package pme123.camundala.camunda

import java.io.InputStream

import pme123.camundala.model.bpmn.StaticFile
import zio.{Managed, Task, TaskManaged, UIO}

import scala.io.{BufferedSource, Source}
import scala.xml.{Elem, XML}

object StreamHelper {
  def inputStream(staticFile: StaticFile): InputStream =
    getClass.getClassLoader.getResourceAsStream(staticFile.pathWithName)

  def xml(staticFile: StaticFile): Task[Elem] =
    xmlSource(staticFile).use(s => Task.effect(XML.load(s.reader())))

  private def xmlSource(staticFile: StaticFile): TaskManaged[BufferedSource] =
    Managed.make(Task.effect(Source.fromResource(staticFile.pathWithName)))(
      is => UIO.succeed(is.close())
    )
}
