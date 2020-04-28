package pme123.camundala.model

trait Extensionable {
  def extensions: Extensions
}

object Extensions {
  val none: Extensions = Extensions()
}

case class Extensions(properties: Map[String, String] = Map.empty)
