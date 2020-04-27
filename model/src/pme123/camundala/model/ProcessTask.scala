package pme123.camundala.model

sealed trait ProcessTask
  extends Identifiable {
  def extensions: Extensions
}

case class ServiceTask(id: String,
                       extensions: Extensions = Extensions.none
                      )
  extends ProcessTask

case class UserTask(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask

object Extensions {
  val none: Extensions = Extensions()
}

case class Extensions(properties: Map[String, String] = Map.empty)