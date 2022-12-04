package camundala.simulation
package custom

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.reflect.ClassTag
import reflect.runtime.universe.TypeTag

type ResultType = Either[ScenarioData, ScenarioData]

case class ScenarioData(
    context: ContextData = ContextData(),
    logEntries: Seq[LogEntry] = Seq.empty
) :
  def log(logLevel: LogLevel, msg: String): ScenarioData =
    copy(logEntries = logEntries :+ LogEntry(logLevel, msg))

  def debug(msg: String): ScenarioData = log(LogLevel.DEBUG, msg)

  def info(msg: String): ScenarioData = log(LogLevel.INFO, msg)

  def warn(msg: String): ScenarioData = log(LogLevel.WARN, msg)

  def error(msg: String): ScenarioData = log(LogLevel.ERROR, msg)

  def withProcessInstanceId(processInstanceId: String): ScenarioData =
    copy(context = context.copy(processInstanceId = processInstanceId))
  def withTaskId(taskId: String): ScenarioData =
    copy(context = context.copy(taskId = taskId))
  def withRequestCount(requestCount: Int): ScenarioData =
    copy(context = context.copy(requestCount = requestCount))

end ScenarioData

object ScenarioData:
  def apply(logEntry: LogEntry): ScenarioData =
    ScenarioData(logEntries = Seq(logEntry))
val notSet = "NotSet"
case class ContextData(
                        requestCount: Int = 0,
                        processInstanceId: String = notSet,
                        taskId: String = notSet,
                      )