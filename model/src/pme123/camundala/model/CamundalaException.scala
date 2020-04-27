package pme123.camundala.model

trait CamundalaException extends Throwable {

  def msg: String

  def cause: Option[Throwable] = None

  override def getMessage: String = msg

  override def getCause: Throwable = cause.orNull
}