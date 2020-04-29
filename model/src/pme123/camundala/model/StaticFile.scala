package pme123.camundala.model

import java.io.InputStream

case class StaticFile(fileName: String, resourcePath: String) {
  def inputStream: InputStream = {
    val r = getClass.getClassLoader.getResourceAsStream(s"$resourcePath/$fileName")
    r
  }
}
