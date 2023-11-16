package camundala.simulation

/**
 *
 * @tparam B Builder for Authentication and preRequest
 */
case class SimulationConfig[B](
                             // define tenant if you have one
                             tenantId: Option[String] = None,
                             // the Camunda Port
                             // there are Requests that wait until the process is ready - like getTask.
                             // the Simulation waits 1 second between the Requests.
                             // so with a timeout of 10 sec it will try 10 times (retryDuration = 1.second)
                             maxCount: Int = 10,
                             // REST endpoint of Camunda
                             endpoint: String = "http://localhost:8080/engine-rest",
                             // you can add authentication with this - default there is none.
                             // see BasicSimulationDsl / OAuthSimulationDsl for examples
                             authHeader: B => B = (b: B) => b,
                             // the maximum LogLevel you want to print the LogEntries.
                             logLevel: LogLevel = LogLevel.INFO
                           ):

  def withTenantId(tenantId: String): SimulationConfig[B] =
    copy(tenantId = Some(tenantId))

  def withMaxCount(maxCount: Int): SimulationConfig[B] =
    copy(maxCount = maxCount)

  def withAuthHeader(authHeader: B => B = b => b): SimulationConfig[B] =
    copy(authHeader = authHeader)

  def withPort(port: Int): SimulationConfig[B] =
    copy(endpoint = s"http://localhost:$port/engine-rest")

  def withLogLevel(logLevel: LogLevel): SimulationConfig[B] =
    copy(logLevel = logLevel)

  lazy val tenantPath: String = tenantId
    .map(id => s"/tenant-id/$id")
    .getOrElse("")