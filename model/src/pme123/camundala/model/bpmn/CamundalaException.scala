package pme123.camundala.model.bpmn

trait CamundalaException extends Throwable {

  def msg: String

  def cause: Option[Throwable] = None

  override def getMessage: String = msg

  override def getCause: Throwable = cause.orNull
}

case class ModelException(msg: String) extends CamundalaException
