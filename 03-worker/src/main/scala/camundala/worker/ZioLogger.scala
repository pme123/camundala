package camundala.worker

import org.slf4j.{Logger, LoggerFactory}
import zio.*
import zio.logging.*
import zio.logging.backend.SLF4J

object ZioLogger:
  val logger = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

end ZioLogger

case class Slf4JLogger(private val delegateLogger: Logger) extends WorkerLogger:

  def debug(message: String): Unit =
    if delegateLogger.isDebugEnabled then
      delegateLogger.debug(message)

  def info(message: String): Unit =
    if delegateLogger.isInfoEnabled then
      delegateLogger.info(message)

  def warn(message: String): Unit =
    if delegateLogger.isWarnEnabled then
      delegateLogger.warn(message)

  def error(err: CamundalaWorkerError): Unit =
    if delegateLogger.isErrorEnabled then
      err.printStackTrace()
      delegateLogger.error(s"Error ${err.causeMsg}")
end Slf4JLogger
object Slf4JLogger:
  def logger(name: String) = Slf4JLogger(LoggerFactory.getLogger(name))
end Slf4JLogger