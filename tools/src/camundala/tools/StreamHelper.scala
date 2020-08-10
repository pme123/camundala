package camundala.tools

import java.io.{ByteArrayInputStream, FileInputStream, InputStream}
import java.nio.file.Paths

import zio._

import scala.io.{BufferedSource, Source}
import scala.xml.{Elem, Node, XML}

case class StreamHelper(basePath: Seq[String]) {

  def inputStream(filePath: Seq[String]): Option[InputStream] = {
    val file = absoluteFilePath(filePath)
    if (file.exists())
      Some(
        new FileInputStream(file)
      ) // prefer from File so in development no restart is needed
    else
      Option(
        getClass.getClassLoader.getResourceAsStream(
          filePathAsPath(filePath).toString
        )
      )
  }

  private def absoluteFilePath(filePath: Seq[String]) = basePath match {
    case Nil =>
      filePathAsPath(filePath).toFile
    case head :: tail =>
      Paths.get(head, tail ++ filePath: _*).toFile
  }

  private def filePathAsPath(filePath: Seq[String]) = filePath match {
    case Nil =>
      Paths.get(".")
    case head :: tail =>
      Paths.get(head, tail: _*)
  }

  def inputStreamM(
      filePath: Seq[String]
  ): IO[StreamHelperException, InputStream] =
    ZIO
      .fromOption(inputStream(filePath))
      .orElseFail(
        StreamHelperException(
          s"Problem loading Static File: ${filePath.mkString("/")}"
        )
      )

  def asString(
      filePath: Seq[String],
      includes: Seq[String] = Seq.empty
  ): Option[String] =
    inputStream(filePath).map((is: InputStream) =>
      includes.mkString("", "\n", "\n") +
        scala.io.Source.fromInputStream(is).mkString("")
    )

  def asStringM(
      filePath: Seq[String],
      includes: Seq[String] = Seq.empty
  ): IO[StreamHelperException, String] =
    ZIO
      .fromOption(asString(filePath, includes))
      .orElseFail(
        StreamHelperException(
          s"Problem loading Static File: ${filePath.mkString("/")}"
        )
      )

  def inputStream(xml: Node): InputStream =
    new ByteArrayInputStream(xml.toString.getBytes)

  def inputStreamManaged(
      filePath: Seq[String]
  ): Option[Managed[Throwable, InputStream]] = {
    inputStream(filePath).map(is =>
      Managed.make(Task.effect(is))(is => UIO.succeed(is.close()))
    )

  }

  def xml(filePath: Seq[String]): Task[Elem] = {
    xmlSource(filePath).use(s =>
      ZIO
        .effect(XML.load(s.reader()))
        .mapError(ex =>
          StreamHelperException(
            s"There is a Problem loading ${filePath.mkString("/")}",
            Some(ex)
          )
        )
    )
  }

  private def xmlSource(filePath: Seq[String]): TaskManaged[BufferedSource] = {
    val file = absoluteFilePath(filePath)
    Managed.make(
      Task(
        if (file.exists())
          Source.fromFile(
            file
          ) // prefer from File so in development no restart is needed
        else
          Source
            .fromResource(Paths.get(filePath.head, filePath.tail: _*).toString)
      )
    )(is => UIO.succeed(is.close()))
  }

  case class StreamHelperException(
      msg: String,
      override val cause: Option[Throwable] = None
  ) extends CamundalaException

}
