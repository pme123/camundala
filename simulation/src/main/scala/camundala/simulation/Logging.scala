package camundala.simulation

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.language.implicitConversions

trait Logging {

  def debug(msg: String): LogEntry = LogEntry(LogLevel.DEBUG, msg)
  def info(msg: String): LogEntry = LogEntry(LogLevel.INFO, msg)
  def warn(msg: String): LogEntry = LogEntry(LogLevel.WARN, msg)
  def error(msg: String): LogEntry = LogEntry(LogLevel.ERROR, msg)

  implicit def toLogSeq(logEntry: LogEntry): Seq[LogEntry] = Seq(logEntry)

  extension (logs: Seq[LogEntry])
    def maxLevel: LogLevel =
      logs.map(_.logLevel).sorted.headOption.getOrElse(LogLevel.DEBUG)

    def filter(logLevel: LogLevel): Seq[LogEntry] =
      logs.filter(_.logLevel.compareTo(logLevel) <= 0)

}

lazy val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

case class LogEntry(logLevel: LogLevel, msg: String, timestamp: LocalDateTime = LocalDateTime.now()):
  self =>
  def toSeq: Seq[LogEntry] = Seq(self)

  override def toString: String =
    s"${dtf.format(timestamp)} $logLevel: $msg"

enum LogLevel(val color: String) extends Ordered[LogLevel] :
  override def compare(that: LogLevel): Int = that.ordinal - ordinal

  case DEBUG extends LogLevel(Console.GREEN)
  case INFO extends LogLevel(Console.GREEN)
  case WARN extends LogLevel(Console.MAGENTA)
  case ERROR extends LogLevel(Console.RED)


